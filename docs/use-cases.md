# S3 호환 객체 저장소 유스케이스

## 1. 버킷 관리 유스케이스

### UC-01: 버킷 생성
**주 액터**: 인증된 사용자
**사전 조건**: 사용자가 유효한 액세스 키로 인증됨
**트리거**: PUT /{bucket-name} 요청

**메인 플로우**:
1. 사용자가 버킷 생성 요청을 보냄
2. 시스템이 AWS Signature V4로 요청 인증
3. 시스템이 버킷명 유효성 검증 (3-63자, 소문자, 숫자, 하이픈만)
4. 시스템이 버킷명 중복 확인
5. 시스템이 버킷을 데이터베이스에 생성
6. 시스템이 성공 응답(200 OK) 반환

**대체 플로우**:
- 3a. 버킷명이 유효하지 않음 → 400 Bad Request
- 4a. 버킷명이 이미 존재 → 409 Conflict
- 2a. 인증 실패 → 401 Unauthorized

**예제 요청**:
```bash
curl -X PUT https://s3.example.com/my-bucket \
  -H "Authorization: AWS4-HMAC-SHA256 ..." \
  -H "x-amz-date: 20240101T000000Z"
```

### UC-02: 버킷 삭제
**주 액터**: 버킷 소유자
**사전 조건**: 버킷이 존재하고 비어있음
**트리거**: DELETE /{bucket-name} 요청

**메인 플로우**:
1. 소유자가 버킷 삭제 요청
2. 시스템이 요청 인증 및 소유권 확인
3. 시스템이 버킷이 비어있는지 확인
4. 시스템이 버킷 메타데이터 삭제
5. 시스템이 204 No Content 반환

**대체 플로우**:
- 2a. 소유자가 아님 → 403 Forbidden
- 3a. 버킷에 객체가 있음 → 409 Conflict (BucketNotEmpty)
- 버킷이 존재하지 않음 → 404 Not Found

### UC-03: 버킷 목록 조회
**주 액터**: 인증된 사용자
**사전 조건**: 사용자 인증됨
**트리거**: GET / 요청

**메인 플로우**:
1. 사용자가 버킷 목록 요청
2. 시스템이 요청 인증
3. 시스템이 사용자 소유 버킷 조회
4. 시스템이 버킷 목록 반환 (JSON/XML)

**응답 예제**:
```json
{
  "buckets": [
    {
      "name": "my-bucket",
      "creationDate": "2024-01-01T00:00:00Z"
    },
    {
      "name": "photos",
      "creationDate": "2024-01-02T00:00:00Z"
    }
  ],
  "owner": {
    "id": "user123",
    "displayName": "John Doe"
  }
}
```

## 2. 객체 관리 유스케이스

### UC-04: 객체 업로드 (단일)
**주 액터**: 버킷 소유자 또는 권한 있는 사용자
**사전 조건**: 대상 버킷 존재
**트리거**: PUT /{bucket}/{key} 요청

**메인 플로우**:
1. 사용자가 객체와 메타데이터 전송
2. 시스템이 요청 인증 및 권한 확인
3. 시스템이 데이터를 4MB 청크로 분할
4. 시스템이 각 청크를 파일시스템에 저장
5. 시스템이 ETag(해시) 계산
6. 시스템이 객체 메타데이터 저장
7. 버전 관리 활성화 시 새 버전 생성
8. 시스템이 ETag와 버전 ID 반환

**대체 플로우**:
- 3a. 스토리지 용량 부족 → 507 Insufficient Storage
- 5a. 체크섬 불일치 → 400 Bad Request
- 7a. 버전 관리 비활성화 → 기존 객체 덮어쓰기

**청크 저장 상세**:
```
입력: 15MB 파일
처리:
- 청크 1: 0-4MB → /storage/bucket/key/v1/chunk-0
- 청크 2: 4-8MB → /storage/bucket/key/v1/chunk-1
- 청크 3: 8-12MB → /storage/bucket/key/v1/chunk-2
- 청크 4: 12-15MB → /storage/bucket/key/v1/chunk-3
```

### UC-05: 객체 다운로드
**주 액터**: 권한 있는 사용자 또는 익명(공개 객체)
**사전 조건**: 객체 존재
**트리거**: GET /{bucket}/{key} 요청

**메인 플로우**:
1. 사용자가 객체 요청
2. 시스템이 권한 확인
3. 시스템이 최신 버전 조회 (또는 특정 버전)
4. 시스템이 청크를 순서대로 스트리밍
5. 시스템이 HTTP 헤더와 함께 데이터 전송

**대체 플로우**:
- 3a. 객체 없음 → 404 Not Found
- 3b. 최신이 삭제 마커 → 404 Not Found
- 4a. Range 헤더 있음 → 부분 다운로드 (206 Partial Content)

**Range 요청 예제**:
```bash
# 첫 1MB만 다운로드
curl -H "Range: bytes=0-1048575" \
  https://s3.example.com/bucket/large-file.zip
```

### UC-06: 객체 삭제
**주 액터**: 객체 소유자
**사전 조건**: 객체 존재
**트리거**: DELETE /{bucket}/{key} 요청

**메인 플로우 (버전 관리 ON)**:
1. 사용자가 삭제 요청
2. 시스템이 인증 및 권한 확인
3. 시스템이 삭제 마커 생성
4. 삭제 마커를 최신 버전으로 설정
5. 시스템이 204 No Content 반환

**메인 플로우 (버전 관리 OFF)**:
1. 사용자가 삭제 요청
2. 시스템이 인증 및 권한 확인
3. 시스템이 객체 메타데이터 삭제
4. 시스템이 청크 파일 삭제 예약
5. 시스템이 204 No Content 반환

### UC-07: 객체 목록 조회
**주 액터**: 버킷 접근 권한 있는 사용자
**사전 조건**: 버킷 존재
**트리거**: GET /{bucket}?list-type=2 요청

**메인 플로우**:
1. 사용자가 목록 요청 (선택: prefix, delimiter, max-keys)
2. 시스템이 권한 확인
3. 시스템이 조건에 맞는 객체 검색
4. 시스템이 페이지네이션 적용 (기본 1000개)
5. 시스템이 결과 반환

**쿼리 파라미터**:
- `prefix`: 특정 접두사로 필터링
- `delimiter`: 폴더 구조 시뮬레이션
- `max-keys`: 반환할 최대 개수 (최대 1000)
- `continuation-token`: 다음 페이지 토큰

**응답 예제**:
```json
{
  "name": "my-bucket",
  "prefix": "photos/",
  "maxKeys": 100,
  "isTruncated": true,
  "contents": [
    {
      "key": "photos/2024/image1.jpg",
      "lastModified": "2024-01-01T00:00:00Z",
      "etag": "d41d8cd98f00b204e9800998ecf8427e",
      "size": 1024000,
      "storageClass": "STANDARD"
    }
  ],
  "nextContinuationToken": "eyJrZXkiOiJwaG90b3MvMjAyNC9pbWFnZTEwMC5qcGciLCJvZmZzZXQiOjEwMH0="
}
```

## 3. 멀티파트 업로드 유스케이스

### UC-08: 멀티파트 업로드 시작
**주 액터**: 인증된 사용자
**사전 조건**: 대상 버킷 존재
**트리거**: POST /{bucket}/{key}?uploads 요청

**메인 플로우**:
1. 사용자가 멀티파트 업로드 시작 요청
2. 시스템이 고유 uploadId 생성
3. 시스템이 업로드 세션 레코드 생성
4. 시스템이 uploadId 반환

**응답**:
```json
{
  "bucket": "my-bucket",
  "key": "large-video.mp4",
  "uploadId": "2~abcd1234-5678-90ef-ghij-klmnopqrstuv"
}
```

### UC-09: 파트 업로드
**주 액터**: 업로드 세션 소유자
**사전 조건**: 활성 업로드 세션 존재
**트리거**: PUT /{bucket}/{key}?partNumber=N&uploadId=... 요청

**메인 플로우**:
1. 사용자가 파트 번호와 데이터 전송 (5MB-5GB)
2. 시스템이 uploadId 유효성 확인
3. 시스템이 파트를 임시 위치에 저장
4. 시스템이 파트 ETag 계산
5. 시스템이 파트 메타데이터 저장
6. 시스템이 ETag 반환

**제약사항**:
- 파트 번호: 1-10000
- 최소 크기: 5MB (마지막 파트 제외)
- 최대 크기: 5GB

### UC-10: 멀티파트 업로드 완료
**주 액터**: 업로드 세션 소유자
**사전 조건**: 모든 파트 업로드 완료
**트리거**: POST /{bucket}/{key}?uploadId=... 요청

**메인 플로우**:
1. 사용자가 파트 목록과 함께 완료 요청
2. 시스템이 모든 파트 검증
3. 시스템이 파트를 순서대로 병합
4. 시스템이 최종 ETag 계산
5. 시스템이 객체 버전 생성
6. 시스템이 임시 파트 삭제
7. 시스템이 위치와 ETag 반환

**요청 본문**:
```json
{
  "parts": [
    { "partNumber": 1, "etag": "abc123" },
    { "partNumber": 2, "etag": "def456" },
    { "partNumber": 3, "etag": "ghi789" }
  ]
}
```

### UC-11: 멀티파트 업로드 중단
**주 액터**: 업로드 세션 소유자
**사전 조건**: 활성 업로드 세션 존재
**트리거**: DELETE /{bucket}/{key}?uploadId=... 요청

**메인 플로우**:
1. 사용자가 중단 요청
2. 시스템이 uploadId 확인
3. 시스템이 모든 임시 파트 삭제
4. 시스템이 업로드 세션 삭제
5. 시스템이 204 No Content 반환

## 4. 버전 관리 유스케이스

### UC-12: 버전 관리 활성화
**주 액터**: 버킷 소유자
**사전 조건**: 버킷 존재
**트리거**: PUT /{bucket}?versioning 요청

**메인 플로우**:
1. 소유자가 버전 관리 설정 요청
2. 시스템이 버킷 설정 업데이트
3. 이후 모든 업로드는 새 버전 생성
4. 시스템이 200 OK 반환

### UC-13: 특정 버전 조회
**주 액터**: 권한 있는 사용자
**사전 조건**: 버전 관리 활성화, 객체 버전 존재
**트리거**: GET /{bucket}/{key}?versionId=... 요청

**메인 플로우**:
1. 사용자가 특정 버전 ID로 요청
2. 시스템이 해당 버전 검색
3. 시스템이 버전 데이터 반환
4. 응답 헤더에 x-amz-version-id 포함

### UC-14: 버전 목록 조회
**주 액터**: 버킷 소유자
**사전 조건**: 버전 관리 활성화
**트리거**: GET /{bucket}?versions 요청

**메인 플로우**:
1. 소유자가 버전 목록 요청
2. 시스템이 모든 버전과 삭제 마커 조회
3. 시스템이 시간순 정렬하여 반환

**응답 예제**:
```json
{
  "versions": [
    {
      "key": "document.pdf",
      "versionId": "v3",
      "isLatest": true,
      "isDeleteMarker": false,
      "lastModified": "2024-01-03T00:00:00Z",
      "size": 2048000
    },
    {
      "key": "document.pdf",
      "versionId": "v2-delete",
      "isLatest": false,
      "isDeleteMarker": true,
      "lastModified": "2024-01-02T00:00:00Z"
    },
    {
      "key": "document.pdf",
      "versionId": "v1",
      "isLatest": false,
      "isDeleteMarker": false,
      "lastModified": "2024-01-01T00:00:00Z",
      "size": 1024000
    }
  ]
}
```

## 5. 태깅 유스케이스

### UC-15: 객체 태그 추가
**주 액터**: 객체 소유자
**사전 조건**: 객체 존재
**트리거**: PUT /{bucket}/{key}?tagging 요청

**메인 플로우**:
1. 사용자가 태그 세트 전송
2. 시스템이 태그 유효성 검증 (최대 10개)
3. 시스템이 기존 태그 대체
4. 시스템이 204 No Content 반환

**요청 본문**:
```json
{
  "tags": [
    { "key": "Environment", "value": "Production" },
    { "key": "Project", "value": "WebApp" },
    { "key": "Owner", "value": "TeamA" }
  ]
}
```

### UC-16: 객체 태그 조회
**주 액터**: 권한 있는 사용자
**사전 조건**: 객체 존재
**트리거**: GET /{bucket}/{key}?tagging 요청

**메인 플로우**:
1. 사용자가 태그 조회 요청
2. 시스템이 현재 버전의 태그 검색
3. 시스템이 태그 목록 반환

## 6. 보안 및 접근 제어 유스케이스

### UC-17: 액세스 키 생성
**주 액터**: 시스템 관리자
**사전 조건**: 사용자 계정 존재
**트리거**: 관리 API 호출

**메인 플로우**:
1. 관리자가 사용자 ID로 키 생성 요청
2. 시스템이 20자 액세스 키 ID 생성 (AKIA...)
3. 시스템이 40자 시크릿 키 생성
4. 시스템이 시크릿 키 해시 저장
5. 시스템이 평문 시크릿 키 한 번만 반환

**응답**:
```json
{
  "accessKeyId": "AKIAIOSFODNN7EXAMPLE",
  "secretAccessKey": "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
  "status": "Active",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### UC-18: AWS Signature V4 인증
**주 액터**: API 클라이언트
**사전 조건**: 유효한 액세스 키 보유
**트리거**: 모든 API 요청

**메인 플로우**:
1. 클라이언트가 Canonical Request 생성
2. 클라이언트가 String to Sign 생성
3. 클라이언트가 시크릿 키로 서명
4. 클라이언트가 Authorization 헤더 추가
5. 시스템이 서명 재계산 및 검증
6. 일치하면 요청 처리 진행

**Authorization 헤더 형식**:
```
AWS4-HMAC-SHA256
Credential=AKIAIOSFODNN7EXAMPLE/20240101/us-east-1/s3/aws4_request,
SignedHeaders=host;x-amz-date,
Signature=fe5f80b2c3d4e7a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5
```

### UC-19: 버킷 정책 설정
**주 액터**: 버킷 소유자
**사전 조건**: 버킷 존재
**트리거**: PUT /{bucket}?policy 요청

**메인 플로우**:
1. 소유자가 JSON 정책 전송
2. 시스템이 정책 문법 검증
3. 시스템이 정책 저장
4. 이후 모든 요청에 정책 적용

**정책 예제**:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::my-bucket/public/*"
    }
  ]
}
```

## 7. 운영 및 모니터링 유스케이스

### UC-20: 만료된 멀티파트 정리
**주 액터**: 시스템 (백그라운드 작업)
**사전 조건**: 없음
**트리거**: 주기적 실행 (매일)

**메인 플로우**:
1. 시스템이 7일 이상 된 미완료 업로드 검색
2. 각 업로드에 대해:
   - 임시 파트 파일 삭제
   - 데이터베이스 레코드 삭제
3. 정리 결과 로깅

### UC-21: 고아 청크 정리
**주 액터**: 시스템 (백그라운드 작업)
**사전 조건**: 없음
**트리거**: 주기적 실행 (매주)

**메인 플로우**:
1. 시스템이 참조 없는 청크 검색
2. 30일 이상 된 고아 청크 삭제
3. 스토리지 공간 회수

### UC-22: 스토리지 사용량 리포트
**주 액터**: 시스템 관리자
**사전 조건**: 관리자 권한
**트리거**: 관리 API 호출

**메인 플로우**:
1. 관리자가 사용량 리포트 요청
2. 시스템이 버킷별 사용량 집계
3. 시스템이 사용자별 사용량 집계
4. 시스템이 스토리지 클래스별 분석
5. 시스템이 리포트 반환

**리포트 예제**:
```json
{
  "summary": {
    "totalBuckets": 150,
    "totalObjects": 1000000,
    "totalSize": "10.5 TB",
    "totalVersions": 1500000
  },
  "byBucket": [
    {
      "name": "videos",
      "objectCount": 5000,
      "totalSize": "5 TB",
      "storageClass": {
        "STANDARD": "4 TB",
        "INFREQUENT_ACCESS": "1 TB"
      }
    }
  ],
  "byUser": [
    {
      "username": "alice",
      "bucketCount": 10,
      "totalSize": "2 TB"
    }
  ]
}
```

## 8. 오류 처리 시나리오

### 네트워크 장애 처리
- **재시도 로직**: 지수 백오프 (1s, 2s, 4s, 8s)
- **멱등성 보장**: PUT, DELETE는 멱등
- **체크섬 검증**: 데이터 무결성 확인

### 용량 제한 처리
- **할당량 관리**: 사용자별/버킷별 제한
- **경고 알림**: 80% 도달 시 알림