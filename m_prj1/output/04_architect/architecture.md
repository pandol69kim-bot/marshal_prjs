# 시스템 아키텍처

## 1. 전체 구성도

```
┌─────────────────────────────────────────────────────────────┐
│                        Client Layer                          │
│   ┌──────────────┐                                          │
│   │  Vue 3 SPA   │  ── Vite Build → Nginx/CDN 배포          │
│   │  (Vite 5)    │                                          │
│   └──────┬───────┘                                          │
└──────────┼──────────────────────────────────────────────────┘
           │ HTTPS (REST API, /api/v1/*)
           │ JWT Bearer Token
           ▼
┌─────────────────────────────────────────────────────────────┐
│                      API Server Layer                        │
│   ┌──────────────────────────────────────────────────────┐  │
│   │           Spring Boot 3.3 (JDK 21)                   │  │
│   │                                                       │  │
│   │  Controller → Service → Repository → JPA → DB        │  │
│   │       │                                               │  │
│   │  Spring Security (JWT + OAuth2)                       │  │
│   │  SpringDoc (OpenAPI 3.0)                              │  │
│   │  Spring Actuator (Health/Metrics)                     │  │
│   └──────────────────────────────────────────────────────┘  │
│         │                    │                    │          │
│    PostgreSQL 16          Redis 7           External APIs    │
│    (Primary DB)         (Cache/Session)    (Feign/WebClient) │
└─────────────────────────────────────────────────────────────┘
           │
           ▼ (외부 시스템 연동)
┌─────────────────────────────────────────────────────────────┐
│                    External Systems                          │
│   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│   │ Google   │  │  Kakao   │  │ SendGrid │  │  AWS S3  │  │
│   │ OAuth2   │  │  OAuth2  │  │  Email   │  │  Minio   │  │
│   └──────────┘  └──────────┘  └──────────┘  └──────────┘  │
│   ┌──────────┐  ┌──────────┐                               │
│   │ NCP SMS  │  │  Slack   │                               │
│   │          │  │ Webhook  │                               │
│   └──────────┘  └──────────┘                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Spring Boot 레이어드 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│ Presentation Layer                                       │
│   @RestController, @RequestMapping                       │
│   - 입력 검증 (@Valid, @Validated)                       │
│   - 공통 응답 포맷 (ApiResponse<T>)                      │
│   - @ControllerAdvice 전역 예외 처리                     │
├─────────────────────────────────────────────────────────┤
│ Application Layer (Service)                              │
│   @Service, @Transactional                               │
│   - 비즈니스 로직 조율                                    │
│   - 외부 연동 서비스 호출                                  │
│   - 도메인 이벤트 발행                                    │
├─────────────────────────────────────────────────────────┤
│ Domain Layer                                             │
│   @Entity, Repository Interface                          │
│   - JPA 엔티티 정의                                       │
│   - 도메인 규칙 캡슐화                                    │
├─────────────────────────────────────────────────────────┤
│ Infrastructure Layer                                     │
│   @Repository, @Component                                │
│   - Spring Data JPA 구현체                               │
│   - Feign Client 구현체                                   │
│   - Redis, S3, 알림 어댑터                               │
└─────────────────────────────────────────────────────────┘
```

---

## 3. 인증 흐름

### 3-1. JWT 이메일 로그인
```
Client                    Spring Boot                    Redis
  │                           │                            │
  │ POST /api/v1/auth/login   │                            │
  │ {email, password}  ───────▶                            │
  │                     (BCrypt 검증)                      │
  │                     (Access Token 생성: 15분)          │
  │                     (Refresh Token 생성: 7일) ────────▶│
  │                           │                  store     │
  │◀─── 200 OK ───────────────│                            │
  │ {accessToken}             │                            │
  │ Set-Cookie: refreshToken (HttpOnly)                    │
```

### 3-2. OAuth2 소셜 로그인
```
Client               Spring Boot              Google/Kakao
  │                       │                        │
  │ GET /oauth2/authorize  │                        │
  │ /google ──────────────▶                        │
  │                 (redirect_uri 생성)             │
  │◀── 302 Redirect ──────│                        │
  │                       │ ◀── Authorization Code ─│
  │                       │ ──── Token Exchange ───▶│
  │                       │ ◀── Access Token ───────│
  │                       │ ──── UserInfo 요청 ─────▶│
  │                       │ ◀── 사용자 정보 ─────────│
  │                  (내부 JWT 발급)                 │
  │◀── 302 /dashboard?token=... ──────────────────  │
```

---

## 4. 외부 연동 아키텍처

### 4-1. Feign Client 패턴 (동기 연동)
```
Service
  │
  ├─▶ ExternalApiClient (Feign Interface)
  │       │
  │       ├─▶ [Resilience4j Circuit Breaker]
  │       │        │
  │       │        ├─ CLOSED: 정상 호출 → External API
  │       │        ├─ OPEN: Fallback 응답 반환
  │       │        └─ HALF_OPEN: 일부 요청으로 복구 시도
  │       │
  │       └─▶ ExternalApiLogAspect (AOP 로깅)
  │
  └─▶ 응답 처리 (외부 에러 → 내부 Exception 변환)
```

### 4-2. WebClient 패턴 (비동기 연동)
```java
webClient.get()
    .uri("/external/data")
    .header("X-API-KEY", apiKey)
    .retrieve()
    .onStatus(HttpStatus::is4xxClientError, 
              resp -> Mono.error(new ExternalApiException(...)))
    .bodyToMono(ExternalResponse.class)
    .timeout(Duration.ofSeconds(5))
    .retryWhen(Retry.backoff(3, Duration.ofMillis(500)))
```

---

## 5. 공통 응답 포맷

### 성공 응답
```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "meta": {
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

### 에러 응답 (RFC 7807 Problem Details)
```json
{
  "success": false,
  "data": null,
  "error": {
    "type": "https://api.example.com/errors/validation",
    "title": "Validation Failed",
    "status": 400,
    "detail": "email 필드는 올바른 이메일 형식이어야 합니다",
    "instance": "/api/v1/auth/login",
    "errors": [
      { "field": "email", "message": "올바른 이메일 형식이 아닙니다" }
    ]
  }
}
```

---

## 6. API 버저닝 전략

```
URL 기반 버저닝:
  /api/v1/resources    (현재)
  /api/v2/resources    (차기, v1 동시 운영 6개월)

Deprecated 헤더:
  Deprecation: true
  Sunset: 2025-12-31
  Link: </api/v2/resources>; rel="successor-version"
```

---

## 7. CORS 설정

```
허용 Origin:   환경변수 ALLOWED_ORIGINS (화이트리스트)
허용 Method:   GET, POST, PUT, PATCH, DELETE, OPTIONS
허용 Header:   Authorization, Content-Type, X-Request-ID
Expose Header: X-Total-Count, X-Request-ID
Credentials:   true (Cookie 기반 Refresh Token)
MaxAge:        3600
```
