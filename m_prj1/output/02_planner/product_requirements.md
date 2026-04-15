# 제품 요구사항 정의서 (PRD)
> **프로젝트**: 외부 시스템 연동 REST API 웹서비스  
> **기술 스택**: Vue 3 + Vite / Spring Boot 3.x + JDK 21

---

## 1. 제품 비전

- **목표**: 외부 시스템과 표준화된 방식으로 연동하고, 일관된 REST API를 제공하는 웹 서비스 구축
- **KPI**: API 응답 P95 < 200ms / 외부 연동 실패율 < 0.1% / 코드 커버리지 ≥ 80%

---

## 2. 기능 요구사항

### Epic 1: 인증/인가 (Authentication & Authorization)

| ID | User Story | 우선순위 | Acceptance Criteria |
|----|-----------|---------|-------------------|
| US-001 | 사용자로서 이메일/비밀번호로 로그인하여 JWT 토큰을 발급받고 싶다 | Must | - Access Token (15분) 발급<br>- Refresh Token (7일) HttpOnly Cookie<br>- 잘못된 자격증명 시 401 반환 |
| US-002 | 사용자로서 Google/Kakao OAuth2로 소셜 로그인을 하고 싶다 | Must | - 소셜 로그인 후 내부 JWT 발급<br>- 신규 사용자 자동 회원가입<br>- 기존 계정 연동 |
| US-003 | 관리자로서 특정 리소스에 역할 기반 접근 제어(RBAC)를 적용하고 싶다 | Must | - ROLE_USER, ROLE_ADMIN 구분<br>- @PreAuthorize 어노테이션으로 권한 체크<br>- 권한 없을 시 403 반환 |

### Epic 2: REST API 설계 표준

| ID | User Story | 우선순위 | Acceptance Criteria |
|----|-----------|---------|-------------------|
| US-010 | 프론트엔드 개발자로서 모든 API 응답이 일관된 형식을 가지길 원한다 | Must | - `{ success, data, error, meta }` 공통 응답 포맷<br>- HTTP 상태 코드 올바른 사용<br>- RFC 7807 에러 포맷 |
| US-011 | 개발자로서 API 버전 관리가 되어 하위 호환성이 보장되길 원한다 | Must | - `/api/v1/` URL 기반 버전<br>- 구버전 deprecated 공지 헤더 |
| US-012 | 개발자로서 목록 API에서 페이지네이션/정렬/필터링이 가능하길 원한다 | Must | - `?page=0&size=20&sort=createdAt,desc`<br>- `?filter[status]=ACTIVE`<br>- 응답에 `meta.totalElements`, `meta.totalPages` |

### Epic 3: 외부 시스템 연동

| ID | User Story | 우선순위 | Acceptance Criteria |
|----|-----------|---------|-------------------|
| US-020 | 개발자로서 외부 REST API를 Feign Client로 선언적으로 호출하고 싶다 | Must | - FeignClient 인터페이스 선언만으로 호출<br>- 외부 API 에러 → 내부 에러로 변환<br>- 타임아웃 설정 |
| US-021 | 개발자로서 외부 API 호출 실패 시 Circuit Breaker가 작동하길 원한다 | Should | - Resilience4j 적용<br>- 실패율 50% 초과 시 Circuit Open<br>- Fallback 응답 반환 |
| US-022 | 관리자로서 이메일/SMS 알림을 외부 서비스(SendGrid, NCP SMS)를 통해 발송하고 싶다 | Should | - 비동기(@Async) 처리<br>- 발송 실패 재시도 로직<br>- 발송 이력 DB 저장 |
| US-023 | 개발자로서 S3 호환 스토리지에 파일을 업로드/다운로드하고 싶다 | Should | - Presigned URL 방식<br>- 파일 타입/크기 검증<br>- CDN URL 반환 |

### Epic 4: Vue 3 프론트엔드

| ID | User Story | 우선순위 | Acceptance Criteria |
|----|-----------|---------|-------------------|
| US-030 | 사용자로서 SPA에서 JWT 인증이 자동으로 처리되길 원한다 | Must | - Axios interceptor로 토큰 자동 첨부<br>- 401 시 자동 토큰 갱신<br>- 갱신 실패 시 로그인 화면 리다이렉트 |
| US-031 | 개발자로서 API 호출 로직을 composable로 재사용하고 싶다 | Must | - `useApi()`, `useAuth()` composable<br>- 로딩/에러 상태 통합 관리 |
| US-032 | 사용자로서 API 에러 발생 시 적절한 피드백을 보고 싶다 | Must | - Toast/Alert 통합 에러 표시<br>- 네트워크 오류 구분 메시지 |

---

## 3. 비기능 요구사항

| 항목 | 요구사항 |
|------|---------|
| 성능 | API 응답 P95 < 200ms, DB 쿼리 < 100ms |
| 보안 | OWASP Top 10 대응, HTTPS only, CORS 화이트리스트 |
| 가용성 | 외부 연동 실패 시 Circuit Breaker로 장애 전파 차단 |
| 문서화 | Swagger/OpenAPI 3.0 자동 생성 |
| 테스트 | 단위/통합 커버리지 ≥ 80% |

---

## 4. 화면 목록 & 흐름

| 화면 ID | 화면명 | 설명 | 연결 화면 |
|---------|--------|------|----------|
| SCR-001 | 로그인 | 이메일/소셜 로그인 | SCR-002 |
| SCR-002 | 대시보드 | 메인 현황 | SCR-003, SCR-004 |
| SCR-003 | 리소스 목록 | 페이지네이션/필터링 | SCR-005 |
| SCR-004 | 외부 연동 현황 | Circuit Breaker 상태 | - |
| SCR-005 | 리소스 상세/편집 | CRUD | SCR-003 |
| SCR-006 | 알림 설정 | 이메일/SMS 설정 | SCR-002 |

---

## 5. 데이터 모델

### Entity: User
| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| id | UUID | Y | PK |
| email | VARCHAR(255) | Y | 이메일 (UNIQUE) |
| password | VARCHAR(255) | N | BCrypt 해시 (소셜 로그인 시 null) |
| name | VARCHAR(100) | Y | 이름 |
| role | ENUM | Y | USER, ADMIN |
| provider | ENUM | Y | LOCAL, GOOGLE, KAKAO |
| providerId | VARCHAR(255) | N | 소셜 Provider ID |
| createdAt | TIMESTAMP | Y | 생성일 |

### Entity: ExternalApiLog
| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| id | BIGINT | Y | PK (auto increment) |
| serviceName | VARCHAR(100) | Y | 외부 서비스명 |
| endpoint | VARCHAR(500) | Y | 호출 URL |
| method | VARCHAR(10) | Y | HTTP Method |
| requestBody | TEXT | N | 요청 바디 |
| responseStatus | INT | Y | 응답 상태 코드 |
| responseBody | TEXT | N | 응답 바디 |
| durationMs | BIGINT | Y | 응답 시간(ms) |
| createdAt | TIMESTAMP | Y | 호출 시각 |

### Entity: NotificationHistory
| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| id | BIGINT | Y | PK |
| userId | UUID | Y | 수신자 |
| channel | ENUM | Y | EMAIL, SMS, SLACK |
| title | VARCHAR(200) | Y | 제목 |
| content | TEXT | Y | 내용 |
| status | ENUM | Y | PENDING, SENT, FAILED |
| sentAt | TIMESTAMP | N | 발송 완료 시각 |

---

## 6. API 명세 초안

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | /api/v1/auth/login | 이메일 로그인 | Public |
| POST | /api/v1/auth/refresh | 토큰 갱신 | Cookie |
| POST | /api/v1/auth/logout | 로그아웃 | Required |
| GET | /api/v1/auth/oauth2/{provider} | 소셜 로그인 시작 | Public |
| GET | /api/v1/users/me | 내 정보 조회 | Required |
| PUT | /api/v1/users/me | 내 정보 수정 | Required |
| GET | /api/v1/resources | 리소스 목록 (페이징) | Required |
| POST | /api/v1/resources | 리소스 생성 | Required |
| GET | /api/v1/resources/{id} | 리소스 상세 | Required |
| PUT | /api/v1/resources/{id} | 리소스 수정 | Required |
| DELETE | /api/v1/resources/{id} | 리소스 삭제 | Required (ADMIN) |
| POST | /api/v1/files/upload | 파일 업로드 URL 발급 | Required |
| POST | /api/v1/notifications/send | 알림 발송 | Required (ADMIN) |
| GET | /api/v1/external/health | 외부 연동 상태 | Required (ADMIN) |

---

## 7. 마일스톤

| Phase | 기간 | 목표 | 주요 기능 |
|-------|------|------|----------|
| Phase 1 | 2주 | 기반 구축 | Spring Boot 초기 설정, JWT 인증, 공통 응답 포맷, Vue 프로젝트 초기화 |
| Phase 2 | 2주 | 핵심 API | CRUD REST API, 페이지네이션, Vue 컴포넌트/composable |
| Phase 3 | 2주 | 외부 연동 | OAuth2 소셜 로그인, Feign Client, 알림 연동 |
| Phase 4 | 1주 | 안정화 | Circuit Breaker, 테스트, Swagger 문서화 |
