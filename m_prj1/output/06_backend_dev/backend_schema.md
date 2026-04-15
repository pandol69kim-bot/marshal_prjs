# DB 스키마 & Flyway 마이그레이션

## Flyway 마이그레이션 파일

### `V1__init_schema.sql`
```sql
-- ============================================
-- V1: 초기 스키마 생성
-- ============================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- gen_random_uuid()

-- 사용자 테이블
CREATE TABLE users (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255),                            -- 소셜 로그인 시 null
    name        VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER'
                CHECK (role IN ('USER', 'ADMIN')),
    provider    VARCHAR(20)  NOT NULL DEFAULT 'LOCAL'
                CHECK (provider IN ('LOCAL', 'GOOGLE', 'KAKAO')),
    provider_id VARCHAR(255),                            -- 소셜 Provider ID
    avatar_url  TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 리소스 테이블 (비즈니스 도메인 예시)
CREATE TABLE resources (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    owner_id    UUID         NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 외부 API 호출 이력
CREATE TABLE external_api_logs (
    id              BIGSERIAL    PRIMARY KEY,
    service_name    VARCHAR(100) NOT NULL,
    endpoint        VARCHAR(500) NOT NULL,
    method          VARCHAR(10)  NOT NULL DEFAULT 'GET',
    request_body    TEXT,
    response_status INT          NOT NULL,
    response_body   TEXT,
    duration_ms     BIGINT       NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 알림 이력
CREATE TABLE notification_histories (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    channel    VARCHAR(20)  NOT NULL CHECK (channel IN ('EMAIL', 'SMS', 'SLACK')),
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
               CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    error_msg  TEXT,
    sent_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 인덱스
CREATE INDEX idx_users_email     ON users(email);
CREATE INDEX idx_users_provider  ON users(provider, provider_id);
CREATE INDEX idx_resources_owner ON resources(owner_id);
CREATE INDEX idx_resources_status ON resources(status) WHERE status != 'DELETED';
CREATE INDEX idx_ext_logs_service ON external_api_logs(service_name, created_at DESC);
CREATE INDEX idx_noti_user       ON notification_histories(user_id, created_at DESC);

-- updated_at 자동 갱신 트리거
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_resources_updated_at
    BEFORE UPDATE ON resources
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
```

### `V2__add_refresh_token_blacklist.sql`
```sql
-- ============================================
-- V2: JWT Refresh Token 블랙리스트 (Redis 대신 DB 사용 시)
-- ============================================

-- Redis 사용 시 이 테이블 불필요 (주석 처리)
-- CREATE TABLE token_blacklist (
--     id         BIGSERIAL    PRIMARY KEY,
--     token_hash VARCHAR(500) NOT NULL UNIQUE,
--     expires_at TIMESTAMPTZ  NOT NULL,
--     created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
-- );
-- CREATE INDEX idx_token_blacklist_hash ON token_blacklist(token_hash);
-- CREATE INDEX idx_token_blacklist_exp  ON token_blacklist(expires_at);

-- 파일 메타데이터
CREATE TABLE file_metadata (
    id           BIGSERIAL    PRIMARY KEY,
    owner_id     UUID         NOT NULL REFERENCES users(id),
    original_name VARCHAR(500) NOT NULL,
    stored_key   VARCHAR(1000) NOT NULL,
    file_url     TEXT          NOT NULL,
    content_type VARCHAR(100)  NOT NULL,
    file_size    BIGINT        NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_file_meta_owner ON file_metadata(owner_id);
```

---

## JPA 엔티티

### `User.java`
```java
package com.example.app.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "password")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider = Provider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 정적 팩토리 메서드
    public static User createLocal(String email, String encodedPassword, String name) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        user.name = name;
        user.provider = Provider.LOCAL;
        return user;
    }

    public static User createOAuth2(String email, String name, Provider provider, String providerId) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.provider = provider;
        user.providerId = providerId;
        return user;
    }

    public void updateProfile(String name, String avatarUrl) {
        this.name = name;
        if (avatarUrl != null) this.avatarUrl = avatarUrl;
    }

    public enum Role { USER, ADMIN }
    public enum Provider { LOCAL, GOOGLE, KAKAO }
}
```

### `Resource.java`
```java
package com.example.app.domain.resource.entity;

import com.example.app.domain.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Resource create(String name, String description, User owner) {
        Resource resource = new Resource();
        resource.name = name;
        resource.description = description;
        resource.owner = owner;
        return resource;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void delete() {
        this.status = Status.DELETED;
    }

    public enum Status { ACTIVE, INACTIVE, DELETED }
}
```
