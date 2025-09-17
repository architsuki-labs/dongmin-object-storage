-- S3 호환 객체 저장소 DDL
-- PostgreSQL 14+ 권장

-- UUID 확장 활성화
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================
-- 1. 사용자 관리 테이블
-- ==========================

-- 사용자 테이블
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 액세스 키 테이블 (AWS 호환 인증)
CREATE TABLE access_keys (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_key_id VARCHAR(20) NOT NULL UNIQUE,  -- AKIA로 시작하는 20자
    secret_key_hash VARCHAR(255) NOT NULL,      -- bcrypt 해시
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE
);

-- ==========================
-- 2. 버킷 관리 테이블
-- ==========================

-- 버킷 테이블
CREATE TABLE buckets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(63) NOT NULL UNIQUE,  -- S3 버킷명 규칙: 3-63자
    owner_id UUID NOT NULL REFERENCES users(id),
    region VARCHAR(30) DEFAULT 'us-east-1',
    versioning_enabled BOOLEAN DEFAULT false,
    access_policy JSONB,
    cors_rules JSONB,
    lifecycle_rules JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 버킷 알림 설정
CREATE TABLE bucket_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    bucket_id UUID NOT NULL REFERENCES buckets(id) ON DELETE CASCADE,
    event_type VARCHAR(100) NOT NULL,  -- s3:ObjectCreated:*, s3:ObjectRemoved:* 등
    destination_arn VARCHAR(500),       -- SNS/SQS/Lambda ARN
    filter_rules JSONB,                 -- 접두사/접미사 필터
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==========================
-- 3. 객체 저장 테이블
-- ==========================

-- 객체 버전 테이블 (핵심)
CREATE TABLE object_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    bucket_id UUID NOT NULL REFERENCES buckets(id) ON DELETE CASCADE,
    object_key VARCHAR(1024) NOT NULL,  -- S3 키 최대 1024자
    version_id VARCHAR(50) NOT NULL UNIQUE DEFAULT uuid_generate_v4()::text,
    size BIGINT NOT NULL,
    content_type VARCHAR(255) DEFAULT 'application/octet-stream',
    etag VARCHAR(255) NOT NULL,         -- MD5 또는 멀티파트 ETag
    storage_class VARCHAR(30) DEFAULT 'STANDARD',  -- STANDARD, INFREQUENT_ACCESS, GLACIER
    is_latest BOOLEAN DEFAULT true,
    is_delete_marker BOOLEAN DEFAULT false,
    metadata JSONB,                      -- 시스템 메타데이터
    user_metadata JSONB,                 -- x-amz-meta-* 헤더
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id)
);

-- 객체 청크 테이블 (4MB 단위)
CREATE TABLE chunks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version_id UUID NOT NULL REFERENCES object_versions(id) ON DELETE CASCADE,
    sequence INTEGER NOT NULL,          -- 청크 순서 (0부터 시작)
    storage_path VARCHAR(500) NOT NULL, -- 실제 파일 경로
    size BIGINT NOT NULL,
    md5_hash VARCHAR(32),
    sha256_hash VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(version_id, sequence)
);

-- 객체 태그 테이블
CREATE TABLE object_tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version_id UUID NOT NULL REFERENCES object_versions(id) ON DELETE CASCADE,
    tag_key VARCHAR(128) NOT NULL,      -- 태그 키 (최대 128자)
    tag_value VARCHAR(256) NOT NULL,    -- 태그 값 (최대 256자)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(version_id, tag_key)
);

-- 객체 잠금 테이블
CREATE TABLE object_locks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    version_id UUID NOT NULL REFERENCES object_versions(id) ON DELETE CASCADE,
    lock_mode VARCHAR(20),              -- COMPLIANCE, GOVERNANCE
    retain_until TIMESTAMP WITH TIME ZONE,
    legal_hold_status VARCHAR(10),      -- ON, OFF
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(version_id)
);

-- ==========================
-- 4. 멀티파트 업로드 테이블
-- ==========================

-- 멀티파트 업로드 관리
CREATE TABLE multipart_uploads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    bucket_id UUID NOT NULL REFERENCES buckets(id) ON DELETE CASCADE,
    object_key VARCHAR(1024) NOT NULL,
    upload_id VARCHAR(100) NOT NULL UNIQUE,  -- S3 업로드 ID
    initiated_by UUID NOT NULL REFERENCES users(id),
    metadata JSONB,
    initiated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP + INTERVAL '7 days')
);

-- 멀티파트 파트
CREATE TABLE multipart_parts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    upload_id UUID NOT NULL REFERENCES multipart_uploads(id) ON DELETE CASCADE,
    part_number INTEGER NOT NULL,       -- 1-10000
    size BIGINT NOT NULL,
    etag VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL, -- 임시 파트 경로
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(upload_id, part_number),
    CHECK(part_number >= 1 AND part_number <= 10000)
);

-- ==========================
-- 5. 인덱스 생성
-- ==========================

-- 성능 최적화 인덱스
CREATE INDEX idx_access_keys_user ON access_keys(user_id);
CREATE INDEX idx_access_keys_active ON access_keys(access_key_id) WHERE is_active = true;

CREATE INDEX idx_buckets_owner ON buckets(owner_id);
CREATE INDEX idx_bucket_notifications_bucket ON bucket_notifications(bucket_id);

-- 객체 조회 최적화
CREATE INDEX idx_object_versions_bucket_key ON object_versions(bucket_id, object_key, created_at DESC);
CREATE UNIQUE INDEX idx_object_versions_latest ON object_versions(bucket_id, object_key)
    WHERE is_latest = true AND is_delete_marker = false;
CREATE INDEX idx_object_versions_version ON object_versions(version_id);

CREATE INDEX idx_chunks_version_seq ON chunks(version_id, sequence);
CREATE INDEX idx_object_tags_version ON object_tags(version_id);
CREATE INDEX idx_object_locks_version ON object_locks(version_id);

-- 멀티파트 업로드 인덱스
CREATE INDEX idx_multipart_uploads_bucket ON multipart_uploads(bucket_id, initiated_at DESC);
CREATE INDEX idx_multipart_uploads_expires ON multipart_uploads(expires_at)
    WHERE completed_at IS NULL;
CREATE INDEX idx_multipart_parts_upload ON multipart_parts(upload_id, part_number);

-- ==========================
-- 6. 트리거 함수
-- ==========================

-- updated_at 자동 업데이트 트리거
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_buckets_updated_at BEFORE UPDATE ON buckets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 새 버전 추가 시 기존 최신 버전 플래그 해제
CREATE OR REPLACE FUNCTION update_object_version_latest()
RETURNS TRIGGER AS $$
BEGIN
    -- 같은 버킷, 같은 키의 기존 최신 버전을 false로 변경
    IF NEW.is_latest = true THEN
        UPDATE object_versions
        SET is_latest = false
        WHERE bucket_id = NEW.bucket_id
            AND object_key = NEW.object_key
            AND id != NEW.id
            AND is_latest = true;
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER manage_object_version_latest
    BEFORE INSERT OR UPDATE ON object_versions
    FOR EACH ROW EXECUTE FUNCTION update_object_version_latest();

-- ==========================
-- 7. 뷰 생성
-- ==========================

-- 최신 객체만 보기 (버전 관리 숨김)
CREATE VIEW current_objects AS
SELECT
    ov.id,
    b.name as bucket_name,
    ov.object_key,
    ov.size,
    ov.content_type,
    ov.etag,
    ov.storage_class,
    ov.metadata,
    ov.user_metadata,
    ov.created_at,
    u.username as created_by
FROM object_versions ov
JOIN buckets b ON ov.bucket_id = b.id
LEFT JOIN users u ON ov.created_by = u.id
WHERE ov.is_latest = true
    AND ov.is_delete_marker = false;

-- 활성 멀티파트 업로드 뷰
CREATE VIEW active_multipart_uploads AS
SELECT
    mu.id,
    b.name as bucket_name,
    mu.object_key,
    mu.upload_id,
    u.username as initiated_by,
    mu.initiated_at,
    mu.expires_at,
    COUNT(mp.id) as parts_uploaded,
    SUM(mp.size) as total_size
FROM multipart_uploads mu
JOIN buckets b ON mu.bucket_id = b.id
JOIN users u ON mu.initiated_by = u.id
LEFT JOIN multipart_parts mp ON mu.id = mp.upload_id
WHERE mu.completed_at IS NULL
GROUP BY mu.id, b.name, mu.object_key, mu.upload_id,
         u.username, mu.initiated_at, mu.expires_at;

-- ==========================
-- 8. 권한 설정 (예시)
-- ==========================

-- 애플리케이션 사용자 생성 (필요시)
-- CREATE USER s3_app WITH PASSWORD 'your_secure_password';

-- 권한 부여
-- GRANT ALL ON ALL TABLES IN SCHEMA public TO s3_app;
-- GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO s3_app;
-- GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO s3_app;

-- ==========================
-- 9. 파티셔닝 (선택사항, 대용량 처리시)
-- ==========================

-- 월별 파티셔닝 예시 (PostgreSQL 12+)
-- CREATE TABLE object_versions_2024_01 PARTITION OF object_versions
--     FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- ==========================
-- 10. 초기 데이터
-- ==========================

-- 기본 관리자 계정 (개발용, 비밀번호: admin123)
INSERT INTO users (username, email, password_hash)
VALUES ('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldmxqq0q3');

-- 기본 액세스 키 생성 (개발용)
INSERT INTO access_keys (user_id, access_key_id, secret_key_hash)
SELECT
    id,
    'AKIAIOSFODNN7EXAMPLE',
    '$2a$10$wJvaZPRxdPWHhbmMmzY8N.RiyMGF5yHCHWy0cEw8JfQRzAEq8WQFK'
FROM users WHERE username = 'admin';

-- ==========================
-- 주석
-- ==========================
COMMENT ON TABLE users IS '시스템 사용자 관리';
COMMENT ON TABLE access_keys IS 'AWS 호환 액세스 키 관리';
COMMENT ON TABLE buckets IS 'S3 호환 버킷 메타데이터';
COMMENT ON TABLE object_versions IS '객체 버전 관리 (핵심 테이블)';
COMMENT ON TABLE chunks IS '대용량 파일용 4MB 청크 저장';
COMMENT ON TABLE object_tags IS '객체별 태그 (키-값 쌍)';
COMMENT ON TABLE object_locks IS '객체 잠금 (컴플라이언스/거버넌스)';
COMMENT ON TABLE multipart_uploads IS '멀티파트 업로드 세션 관리';
COMMENT ON TABLE multipart_parts IS '멀티파트 업로드 개별 파트';
COMMENT ON TABLE bucket_notifications IS '버킷 이벤트 알림 설정';

COMMENT ON COLUMN object_versions.is_latest IS '최신 버전 여부 (버킷-키 조합당 하나만 true)';
COMMENT ON COLUMN object_versions.is_delete_marker IS '삭제 마커 여부 (버전 관리 활성 시 소프트 삭제)';
COMMENT ON COLUMN chunks.sequence IS '청크 순서 (0부터 시작, 순차 읽기용)';
COMMENT ON COLUMN multipart_parts.part_number IS '파트 번호 (1-10000, S3 규격)';