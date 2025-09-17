# S3 호환 객체 저장소 완전판 ERD

## 전체 엔티티 관계도

```mermaid
erDiagram
    users {
        uuid id PK "기본키"
        string username UK "사용자명(유니크)"
        string email UK "이메일(유니크)"
        string password_hash "비밀번호 해시"
        timestamp created_at "생성일시"
        timestamp updated_at "수정일시"
    }

    access_keys {
        uuid id PK "기본키"
        uuid user_id FK "사용자 ID"
        string access_key_id UK "액세스 키 ID"
        string secret_key_hash "시크릿 키 해시"
        boolean is_active "활성화 여부"
        timestamp created_at "생성일시"
        timestamp last_used "마지막 사용일시"
        timestamp expires_at "만료일시"
    }

    buckets {
        uuid id PK "기본키"
        string name UK "버킷명(전역 유니크)"
        uuid owner_id FK "소유자 ID"
        string region "리전"
        boolean versioning_enabled "버전 관리 활성화"
        json access_policy "접근 정책"
        json cors_rules "CORS 규칙"
        json lifecycle_rules "수명주기 규칙"
        timestamp created_at "생성일시"
        timestamp updated_at "수정일시"
    }

    object_versions {
        uuid id PK "기본키"
        uuid bucket_id FK "버킷 ID"
        string object_key "객체 키"
        string version_id UK "버전 ID"
        bigint size "크기(바이트)"
        string content_type "콘텐츠 타입"
        string etag "ETag"
        string storage_class "스토리지 클래스"
        boolean is_latest "최신 버전 여부"
        boolean is_delete_marker "삭제 마커 여부"
        json metadata "시스템 메타데이터"
        json user_metadata "사용자 메타데이터"
        timestamp created_at "생성일시"
        uuid created_by FK "생성자"
    }

    chunks {
        uuid id PK "기본키"
        uuid version_id FK "버전 ID"
        int sequence "순서"
        string storage_path "저장 경로"
        bigint size "크기(바이트)"
        string md5_hash "MD5 해시"
        string sha256_hash "SHA256 해시"
        timestamp created_at "생성일시"
    }

    object_tags {
        uuid id PK "기본키"
        uuid version_id FK "버전 ID"
        string tag_key "태그 키"
        string tag_value "태그 값"
        timestamp created_at "생성일시"
    }

    multipart_uploads {
        uuid id PK "기본키"
        uuid bucket_id FK "버킷 ID"
        string object_key "객체 키"
        string upload_id UK "업로드 ID"
        uuid initiated_by FK "업로드 시작자"
        json metadata "메타데이터"
        timestamp initiated_at "시작일시"
        timestamp completed_at "완료일시"
        timestamp expires_at "만료일시"
    }

    multipart_parts {
        uuid id PK "기본키"
        uuid upload_id FK "업로드 ID"
        int part_number "파트 번호"
        bigint size "크기(바이트)"
        string etag "ETag"
        string storage_path "저장 경로"
        timestamp uploaded_at "업로드일시"
    }

    bucket_notifications {
        uuid id PK "기본키"
        uuid bucket_id FK "버킷 ID"
        string event_type "이벤트 타입"
        string destination_arn "대상 ARN"
        json filter_rules "필터 규칙"
        boolean is_active "활성화 여부"
        timestamp created_at "생성일시"
    }

    object_locks {
        uuid id PK "기본키"
        uuid version_id FK "버전 ID"
        string lock_mode "잠금 모드"
        timestamp retain_until "보존 기한"
        string legal_hold_status "법적 보존 상태"
        timestamp created_at "생성일시"
    }

    users ||--o{ access_keys : "보유"
    users ||--o{ buckets : "소유"
    users ||--o{ object_versions : "생성"
    buckets ||--o{ object_versions : "포함"
    buckets ||--o{ multipart_uploads : "진행중"
    buckets ||--o{ bucket_notifications : "알림설정"
    object_versions ||--o{ chunks : "청크저장"
    object_versions ||--o{ object_tags : "태그"
    object_versions ||--o| object_locks : "잠금"
    users ||--o{ multipart_uploads : "시작"
    multipart_uploads ||--o{ multipart_parts : "파트"
```

## 테이블 상세 설명

### 핵심 테이블

#### users (사용자)
- 사용자 계정 관리
- `id`: UUID 기본키
- `username`: 고유 사용자명
- `email`: 이메일 주소
- `password_hash`: Bcrypt 해시된 비밀번호
- 감사용 타임스탬프

#### access_keys (액세스 키)
- AWS 호환 인증 자격증명
- `access_key_id`: 공개 액세스 키 (20자, 예: AKIA...)
- `secret_key_hash`: 해시된 시크릿 키
- `is_active`: 키 활성화/비활성화
- `expires_at`: 선택적 만료 시간
- `last_used`: 사용 추적

#### buckets (버킷)
- S3 호환 버킷 (전체 기능)
- `name`: 전역적으로 고유한 버킷명
- `versioning_enabled`: 객체 버전 관리 플래그
- `access_policy`: 버킷 정책 JSON
- `cors_rules`: CORS 설정
- `lifecycle_rules`: 객체 수명주기 정책

### 객체 저장 테이블

#### object_versions (객체 버전)
- 완전한 버전 관리
- `object_key`: 버킷 내 객체 경로
- `version_id`: 고유 버전 식별자
- `is_latest`: 현재 버전 플래그
- `is_delete_marker`: 소프트 삭제 표시
- `storage_class`: STANDARD/INFREQUENT_ACCESS/GLACIER
- `metadata`: 시스템 메타데이터
- `user_metadata`: x-amz-meta-* 헤더

#### chunks (청크)
- 대용량 객체용 4MB 데이터 청크
- `version_id`: 특정 객체 버전 연결
- `sequence`: 청크 순서 (0부터 시작)
- `storage_path`: 파일시스템 위치
- 무결성 검증용 해시값

#### object_tags (객체 태그)
- 버전별 키-값 태그
- S3 객체 태깅 API 지원
- 수명주기 규칙 및 접근 제어에 사용

### 업로드 관리

#### multipart_uploads (멀티파트 업로드)
- 진행 중인 멀티파트 업로드
- `upload_id`: S3 호환 식별자
- `expires_at`: 자동 중단 타임스탬프
- `metadata`: 업로드 메타데이터

#### multipart_parts (멀티파트 파트)
- 개별 업로드 파트
- `part_number`: 1-10000 범위
- `storage_path`: 임시 파트 위치
- 완료 시 병합

### 고급 기능

#### bucket_notifications (버킷 알림)
- 이벤트 알림 설정
- `event_type`: s3:ObjectCreated:* 등
- `destination_arn`: SNS/SQS/Lambda 대상
- `filter_rules`: 접두사/접미사 필터

#### object_locks (객체 잠금)
- 컴플라이언스 및 거버넌스 모드
- `lock_mode`: COMPLIANCE/GOVERNANCE
- `retain_until`: 보존 날짜
- `legal_hold_status`: ON/OFF

## 구현 참고사항

### 버전 관리 전략
1. **신규 업로드**: 새 버전 생성, `is_latest=true` 설정, 이전 버전은 `false`로 변경
2. **삭제 (버전관리 ON)**: `is_delete_marker=true`인 삭제 마커 생성
3. **삭제 (버전관리 OFF)**: 모든 버전 하드 삭제
4. **최신 가져오기**: `is_latest=true` AND `is_delete_marker=false` 필터링

### 멀티파트 업로드 플로우
1. **시작**: `upload_id`로 업로드 레코드 생성
2. **파트 업로드**: 각 파트를 임시 경로에 저장
3. **완료**:
   - 순서대로 파트 병합
   - 새 객체 버전 생성
   - 임시 파트 정리
4. **중단**: 업로드 레코드와 모든 파트 제거

### 저장 경로 구조
```
/storage/
├── objects/
│   ├── {bucket-id}/
│   │   └── {object-key-hash[0:2]}/
│   │       └── {object-key-hash[2:4]}/
│   │           └── {version-id}
└── temp/
    └── multipart/
        └── {upload-id}/
            └── part-{number}
```

### 보안 고려사항
- 시크릿 키는 평문으로 저장하지 않음
- `expires_at`을 통한 액세스 키 순환 지원
- 세밀한 접근 제어를 위한 JSON 버킷 정책
- 컴플라이언스 요구사항을 위한 객체 잠금

### S3 API 호환성
- 기본 CRUD 작업
- 버전 관리 지원
- 멀티파트 업로드
- 객체 태깅
- 접근 제어 (IAM 스타일)
- 이벤트 알림 (스키마만)
- 객체 잠금/보존
- 교차 리전 복제 (미포함)
- S3 Select (미포함)
- 배치 작업 (미포함)

이 ERD는 Alex Xu의 S3 호환 객체 저장소 구현을 위한 모든 필수 기능을 제공합니다.