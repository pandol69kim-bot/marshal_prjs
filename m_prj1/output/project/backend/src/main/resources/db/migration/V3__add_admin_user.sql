-- ============================================
-- V3: 초기 어드민 계정 추가
-- ============================================
-- 비밀번호: Admin1234!
-- pgcrypto crypt('bf', 10) → Spring BCryptPasswordEncoder 호환

INSERT INTO users (email, password, name, role, provider)
VALUES (
    'admin@example.com',
    crypt('Admin1234!', gen_salt('bf', 10)),
    '관리자',
    'ADMIN',
    'LOCAL'
)
ON CONFLICT (email) DO NOTHING;
