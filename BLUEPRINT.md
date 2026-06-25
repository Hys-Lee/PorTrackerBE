# PorTracker BE 개발 블루프린트 (BLUEPRINT.md)

본 문서는 인메모리 SQLite 전환, Kafka WAL 복구 아키텍처, Redis 캐시/Rate Limiter, 서킷 브레이커 및 리트라이 고도화 작업을 실현하기 위한 개발 로드맵과 마일스톤을 기술합니다.

---

## 📅 개발 마일스톤 (Milestones)

### 📌 Phase 1. In-Memory SQLite 격리 및 구글 드라이브 백업/복원 연동
- **목표**: 서버 디스크에 임시 파일을 생성하지 않고 서버 RAM 내에서 사용자별 독립 SQLite 인스턴스를 동적으로 운용합니다.
- **주요 태스크**:
  - `SqliteDatabaseManager` 개편: 디스크 경로 대신 `jdbc:sqlite:file:memdb_{userId}?mode=memory&cache=shared` 기반으로 HikariDataSource 동적 생성.
  - `SyncService` 내 백업/복원 API 고도화: `org.sqlite.SQLiteConnection`의 native Backup API를 활용해 메모리 상태의 DB를 바이트 배열로 직렬화하여 구글 드라이브 업/다운로드 처리.

### 📌 Phase 2. Kafka WAL(Write-Ahead Log) 파이프라인 및 복구(Redo) 시스템 구축
- **목표**: 인메모리 데이터의 휘발성 문제를 카프카 토픽에 영속 로깅하고 복구 시점에 Replay하여 해결합니다.
- **주요 태스크**:
  - Kafka Cluster 연동 설정: 런타임에 외부 카프카 브로커 접속 정보 주입.
  - CUD 시점 로그 발행: 서비스단 쓰기 액션 발생 시 `userId`를 메시지 Key로 삼아 `user-transaction-logs` 토픽에 동기식(`Sync Send` / Ack=all)으로 발행.
  - 복구(Redo) 구현: 서버 부팅(또는 유저 가입/로그인) 시, Supabase의 `last_sync_offset` 값 이후의 카프카 메시지들을 오프셋 필터링하여 순차 소비 후 SQLite 메모리 DB에 SQL 실행(Replay).
  - 동기화 시 Supabase에 최종 백업 완료 시점의 카프카 오프셋 값인 **`last_sync_offset` (BIGINT)** 및 구글 파일 `MD5 Checksum` 더블체크 메타를 기록하여 버전 체크포인트로 관리.

### 📌 Phase 3. Supabase 버전 매핑을 통한 Flyway 검증 오버헤드 최적화 (0ms)
- **목표**: 런타임에 데이터베이스가 로드될 때마다 발생하는 Flyway 마검증 오버헤드를 0ms로 스킵합니다.
- **주요 태스크**:
  - 서버 설정 파일에 `APP_DB_VERSION` 환경변수 연동.
  - 유저 DB 로드 시점 검증: Supabase `profile` 테이블의 `user_db_version`과 `APP_DB_VERSION` 비교.
  - 버전 일치 시 `flyway.migrate()` 실행을 생략하고 즉시 커넥션 서빙. 버전 불일치(앱 업데이트 후 첫 접속) 시에만 딱 1회 Flyway 검증 및 마이그레이션 실행 후 Supabase 정보 업데이트.

### 📌 Phase 4. Resilience4j 서킷 브레이커 및 유연한 Fallback 구현
- **목표**: 외부 구글 드라이브 API 장애 시 서버 스레드 잠식을 막고 시스템 가용성을 보장합니다.
- **주요 태스크**:
  - Resilience4j 의존성 주입 및 application.yml 설정.
  - `SyncService` 내 구글 드라이브 연동 메서드에 `@CircuitBreaker` 지정.
  - 다운로드 실패 시 Redis 캐시 스냅샷 데이터를 조회해 보여주는 Fallback 로직 구현.
  - Upload 실패 시 "로컬 보존 중" 유예 상태로 처리하는 Fallback 로직 구현.

### 📌 Phase 5. Upstash Redis 캐싱 및 API Rate Limiter 도입
- **목표**: API 요금 최적화 및 쿼터 제한 준수를 위해 메모리 낭비를 줄인 캐시와 유량 제어를 적용합니다.
- **주요 태스크**:
  - 자주 변경되지 않는 마스터 공통 데이터 및 7일 자산 변동 요약본 캐싱 (Cache-Aside 패턴).
  - 첫 화면 노출 범위의 **2배 용량 조회 데이터 Redis 사전 캐싱** 및 프론트엔드 선요청(Prefetch) 연동 설계.
  - Spring Security Filter Chain에 커스텀 `RateLimitingFilter` 추가.
  - Redis Token Bucket을 이용해 유저당/IP당 분당 API 호출 제한 처리 및 임계치 초과 시 `429 Too Many Requests` 리턴.

### 📌 Phase 6. 지수 백오프(Exponential Backoff) Retry 연동
- **목표**: 비동기 업로드 실패 시 구글 API 서버에 부하를 주지 않고 복원력을 극대화합니다.
- **주요 태스크**:
  - Spring Retry 의존성 설정.
  - 구글 드라이브 비동기 업로드 로직에 지수 백오프 정책 적용 (기본 대기 2s, 배수 2.0, 최대 대기 60s).

### 📌 Phase 7. 롤업(Rollup) 중간 집계 테이블 및 DB 트리거 연동
- **목표**: 대량 데이터 통계 연산 시 발생하는 성능 지연을 중간 집계 테이블과 트리거를 통해 증분 업데이트하여 최소화합니다.
- **주요 태스크**:
  - 통계용 중간 집계 **롤업(Rollup) 테이블** 구성.
  - SQLite(개별 통계) 및 Supabase(전체 통계)에 데이터 변경 감지용 **트리거(Trigger) 및 트리거 함수**를 결합하여 실시간 전체 연산을 배제하고 점진적 증분 업데이트 처리.

### 📌 Phase 8. AES-256-GCM 기반 OAuth 토큰 및 카프카 WAL 로그 암호화
- **목표**: 침해 사고 시에도 중요 자격 증명 및 자산 변경 이력이 유출되지 않도록 영속 데이터를 암호화합니다.
- **주요 태스크**:
  - 대칭키 암호화 유틸리티(`EncryptionUtils`) 구현 (보안 강도가 검증된 AES-256-GCM 알고리즘 및 고유 IV 사용).
  - Supabase `credential` 테이블에 토큰 저장/조회 시 암·복호화 적용.
  - Kafka WAL 로그 발행 시 JSON 페이로드 자체를 대칭키로 암호화하고, Replay 복구 시 복호화하여 가공하도록 변경.
  - 대칭키는 환경변수 `APP_ENCRYPTION_KEY`를 통해 주입받도록 제어.

### 📌 Phase 9. Docker 비루트(Non-root) 컨테이너 가동을 통한 시스템 격리
- **목표**: 백엔드 애플리케이션의 보안 결함으로 컨테이너가 뚫리더라도, 호스트 서버 전체 제어권이 탈취되는 Container Escape 위협을 막습니다.
- **주요 태스크**:
  - Dockerfile에 전용 시스템 유저/그룹 생성 및 디렉토리 권한 부여 (`RUN useradd -u 1001 portracker...`).
  - Spring Boot 어플리케이션이 호스트 커널 루트가 아닌 해당 일반 사용자로 실행되도록 빌드 최적화 (`USER portracker`).
  - 컨테이너 내부의 쓰기 가능 디렉토리(`/db`, `/logs` 등)에 대한 권한 최소화(Least Privilege) 설정.

### 📌 Phase 10. SpEL 동적 키 및 waitTime=0 중복 클릭 방지 분산락 고도화
- **목표**: 분산 락의 키 구성을 선언적으로 고도화하고, 빠른 요청 연타 시의 트랜잭션 오동작을 미연에 강력히 차단합니다.
- **주요 태스크**:
  - AOP 기반 `@DistributedLock` 어노테이션에 SpEL 해석 유틸(`CustomSpringELParser`)을 결합하여 매개변수 바인딩 동적 키 지원.
  - `waitTime = 0` 방식을 통한 중복 제출 방지(Double Submit Prevention) 구조 구현: 락 획득 즉각 실패 시 대기 없이 예외(`TOO_MANY_REQUESTS`) 처리.

