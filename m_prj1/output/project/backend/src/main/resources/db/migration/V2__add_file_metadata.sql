-- ============================================
-- V2: 파일 메타데이터 테이블 추가
-- ============================================

CREATE TABLE file_metadata (
    id             BIGSERIAL     PRIMARY KEY,
    owner_id       UUID          NOT NULL REFERENCES users(id),
    original_name  VARCHAR(500)  NOT NULL,
    stored_key     VARCHAR(1000) NOT NULL,
    file_url       TEXT          NOT NULL,
    content_type   VARCHAR(100)  NOT NULL,
    file_size      BIGINT        NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_file_meta_owner ON file_metadata(owner_id);
