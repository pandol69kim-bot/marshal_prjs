-- ============================================
-- V1: 초기 스키마 생성
-- ============================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 사용자 테이블
CREATE TABLE users (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255),
    name        VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER'
                CHECK (role IN ('USER', 'ADMIN')),
    provider    VARCHAR(20)  NOT NULL DEFAULT 'LOCAL'
                CHECK (provider IN ('LOCAL', 'GOOGLE', 'KAKAO')),
    provider_id VARCHAR(255),
    avatar_url  TEXT,
    created_at  TIMESTAMP  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP  NOT NULL DEFAULT NOW()
);

-- 리소스 테이블
CREATE TABLE resources (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                CHECK (status IN ('ACTIVE', 'INACTIVE', 'DELETED')),
    owner_id    UUID         NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at  TIMESTAMP  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP  NOT NULL DEFAULT NOW()
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
    created_at      TIMESTAMP  NOT NULL DEFAULT NOW()
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
    sent_at    TIMESTAMP,
    created_at TIMESTAMP  NOT NULL DEFAULT NOW()
);

-- 인덱스
CREATE INDEX idx_users_email      ON users(email);
CREATE INDEX idx_users_provider   ON users(provider, provider_id);
CREATE INDEX idx_resources_owner  ON resources(owner_id);
CREATE INDEX idx_resources_status ON resources(status) WHERE status != 'DELETED';
CREATE INDEX idx_ext_logs_service ON external_api_logs(service_name, created_at DESC);
CREATE INDEX idx_noti_user        ON notification_histories(user_id, created_at DESC);

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
