# API Service — Vue 3 + Spring Boot 3.3

외부 시스템 연동 REST API 웹서비스 프로젝트입니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Frontend | Vue 3 + Vite + TypeScript + Tailwind CSS |
| Backend | Spring Boot 3.3 + JDK 21 |
| DB | PostgreSQL 16 + Flyway |
| Cache | Redis 7 |
| 인증 | JWT + OAuth2 (Google, Kakao) |
| 외부 연동 | Feign Client + WebClient + Resilience4j |
| 스토리지 | AWS S3 / MinIO |

## 프로젝트 구조

```
project/
├── frontend/   # Vue 3 + Vite
└── backend/    # Spring Boot 3.3
```

## 빠른 시작

### 1. 인프라 실행

```bash
cd backend
docker-compose up -d
```

### 2. 백엔드 실행

```bash
cd backend
cp .env.example .env
# .env 파일에서 필수 값 설정 후
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. 프론트엔드 실행

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

### 접속

| 서비스 | URL |
|--------|-----|
| 프론트엔드 | http://localhost:5173 |
| 백엔드 API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| MinIO Console | http://localhost:9001 |

## 개발 단계별 착수 순서

1. **Phase 1** (2주) — Spring Boot 초기 설정, Flyway 스키마, JWT 인증, Vue + Axios interceptor
2. **Phase 2** (2주) — CRUD REST API + 페이지네이션, Vue 페이지/컴포넌트, TanStack Query
3. **Phase 3** (2주) — OAuth2 소셜 로그인, Feign Client 외부 연동, 알림 서비스
4. **Phase 4** (1주) — Circuit Breaker, 테스트 커버리지 80%+, Swagger 문서화
