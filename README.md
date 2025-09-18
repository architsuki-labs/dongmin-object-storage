# S3-Lite: Object Storage System Implementation

> Alex Xu의 "System Design Interview Volume 2" 9장을 기반으로 한 S3 호환 객체 저장소 직접 구현 프로젝트

## 프로젝트 개요

**S3-Lite**는 Amazon S3와 유사한 객체 저장소를 **바닥부터 직접 구현**하는 학습 프로젝트입니다. MinIO나 기존 솔루션을 사용하지 않고, Alex Xu의 시스템 설계 원칙을 따라 **Raw Storage Engine**을 직접 개발합니다.

### 프로젝트 목표
- **대규모 시스템 설계** 실전 경험
- **분산 저장소 아키텍처** 이해
- **메타데이터와 데이터 분리** 설계 학습
- **확장 가능한 시스템** 구축 방법론 습득

### 핵심 특징
- **청크 기반 저장** (4MB 단위 분할)
- **멀티파트 업로드** 지원
- **데이터 무결성** 보장 (MD5/SHA256 체크섬)
- **메타데이터 캐싱** (Redis)
- **모니터링 대시보드** (Prometheus + Grafana)

## 시스템 아키텍처

### High-Level Architecture
```
[클라이언트]
    ↓ HTTP/HTTPS
[로드 밸런서 (Nginx)]
    ↓
[API 게이트웨이 (Spring Boot)]
    ↓
┌─────────────────────────────────────┐
│          Control Plane              │
│  [메타데이터 서비스 + PostgreSQL]    │
│  [캐싱 계층 + Redis]                │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│            Data Plane               │
│  [데이터 서비스 - 직접 구현]         │
│  ├── 청크 관리자                    │
│  ├── 복제 관리자                    │
│  ├── 저장소 노드 관리자              │
│  └── 파일시스템 인터페이스           │
└─────────────────────────────────────┘
```

### 데이터 플로우
```
파일 업로드:
Client → API Gateway → 메타데이터 생성 → 데이터 청킹 → 디스크 저장 → 메타데이터 업데이트

파일 다운로드:
Client → API Gateway → 메타데이터 조회 → 청크 수집 → 스트림 합성 → Client
```

## 기술 스택

### Backend
- **언어**: Kotlin
- **프레임워크**: Spring Boot 3.x
- **빌드 도구**: Gradle
- **데이터베이스**: PostgreSQL 15 (메타데이터 전용)
- **캐싱**: Redis 7 (메타데이터 캐싱)

### Infrastructure
- **컨테이너**: Docker + Docker Compose
- **프록시**: Nginx (로드밸런싱 + SSL)
- **모니터링**: Prometheus + Grafana
- **로깅**: Logback + JSON 구조화

### Storage Strategy
- **청킹**: 4MB 단위 파일 분할
- **디렉토리 구조**: 3단계 해시 기반 분산 (`aa/bb/cc/`)
- **무결성**: MD5 + SHA256 체크섬
- **복제**: 추후 Multi-Node 확장 시 구현

## 데이터 모델

### 핵심 엔티티 관계
```
User (1) ──── (N) Bucket (1) ──── (N) Object (1) ──── (N) Chunk
                    │
                    └── (N) MultipartUpload (1) ──── (N) MultipartPart
```

### 저장소 구조
```
PostgreSQL: 메타데이터만 저장
- 버킷 정보 (이름, 소유자, 권한)
- 객체 메타데이터 (크기, 타입, 청크 정보)
- 청크 맵핑 (순서, 위치, 해시)

File System: 실제 데이터 저장
- /storage/chunks/aa/bb/cc/{chunk-uuid}
- 4MB 단위 청크 파일들
- 3단계 디렉토리로 분산 (확장성)
```

## 주요 기능

- [ ] **기본 CRUD**: PUT, GET, DELETE Object
- [ ] **버킷 관리**: 생성, 삭제, 목록 조회
- [ ] **청크 시스템**: 4MB 단위 분할 저장
- [ ] **메타데이터 분리**: PostgreSQL 기반
- [ ] **체크섬 검증**: MD5/SHA256 무결성 확인
- [ ] **기본 인증**: JWT 토큰 기반
- [ ] **멀티파트 업로드**: 대용량 파일 처리
- [ ] **Range Request**: 부분 다운로드 지원
- [ ] **S3 호환 API**: AWS SDK 완전 호환

### 향후 계획
- [ ] **복제 시스템**: Multi-Node 데이터 복제
- [ ] **Erasure Coding**: 저장 공간 효율화
- [ ] **압축**: 자동 데이터 압축
- [ ] **암호화**: 저장/전송 시 암호화
- [ ] **생명주기 관리**: 자동 아카이빙/삭제
- [ ] **AWS S3 Signature v4**: 완전한 S3 호환성

## 핵심 플로우

### 파일 업로드 프로세스
```
1. 클라이언트 → PUT /bucket/object.txt (파일 데이터)
2. API Gateway → 인증/인가 확인
3. 메타데이터 서비스 → 객체 메타데이터 사전 생성
4. 청크 관리자 → 4MB 단위로 데이터 분할
5. 저장소 → 각 청크를 디스크에 저장
6. 체크섬 계산 → MD5/SHA256 해시 생성
7. 메타데이터 업데이트 → 청크 위치 정보 저장
8. 클라이언트 ← 성공 응답
```

### 파일 다운로드 프로세스
```
1. 클라이언트 → GET /bucket/object.txt
2. API Gateway → 인증/인가 확인  
3. 메타데이터 조회 → 객체 존재 확인 + 청크 정보 수집
4. 청크 수집 → 순서대로 청크 파일들 읽기
5. 스트림 합성 → 여러 청크를 하나의 스트림으로 병합
6. 클라이언트 ← 파일 데이터 스트리밍
```

## API 테스트
```bash
# 버킷 생성
curl -X PUT "http://localhost:8080/api/v1/test-bucket"

# 파일 업로드
curl -X PUT "http://localhost:8080/api/v1/test-bucket/hello.txt" \
  -H "Content-Type: text/plain" \
  --data "Hello, S3-Lite!"

# 파일 다운로드
curl "http://localhost:8080/api/v1/test-bucket/hello.txt"
```

### 모니터링 확인
- **API 상태**: http://localhost:8080/actuator/health
- **Grafana 대시보드**: http://localhost:3000 (admin/admin)
- **Prometheus 메트릭**: http://localhost:9090

## 성능 벤치마크

### 목표 성능 지표
- **처리량**: 100MB/s (단일 노드)
- **동시 연결**: 1,000개
- **메타데이터 응답**: < 10ms
- **데이터 업로드**: < 50MB/s
- **가용성**: 99.9% (8시간/월 다운타임)

### 확장성 계획
- **수직 확장**: CPU/메모리 업그레이드
- **수평 확장**: 다중 노드 클러스터링
- **스토리지 확장**: RAID 구성 + 추가 디스크

### 흐름도
```
Controller → Facade → Mapper → Domain Service → Repository
    ↓          ↓         ↓            ↓             ↓
   DTO    →   DTO   →  Command  →   Result    →   Entity
```