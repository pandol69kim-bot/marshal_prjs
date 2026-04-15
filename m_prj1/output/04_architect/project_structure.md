# 프로젝트 디렉토리 구조

## 모노레포 구조

```
project-root/
├── frontend/                          # Vue 3 + Vite
│   ├── index.html
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── tailwind.config.js
│   └── src/
│       ├── main.ts
│       ├── App.vue
│       ├── router/
│       │   └── index.ts               # Vue Router (route guards 포함)
│       ├── stores/                    # Pinia
│       │   ├── auth.store.ts
│       │   └── notification.store.ts
│       ├── composables/               # 재사용 로직
│       │   ├── useApi.ts              # 공통 API 호출 composable
│       │   ├── useAuth.ts             # 인증 composable
│       │   ├── usePagination.ts       # 페이지네이션
│       │   └── useToast.ts            # 알림
│       ├── lib/
│       │   ├── axios.ts               # Axios 인스턴스 + Interceptor
│       │   └── queryClient.ts         # TanStack Query 설정
│       ├── api/                       # API 함수
│       │   ├── auth.api.ts
│       │   ├── resources.api.ts
│       │   └── notifications.api.ts
│       ├── pages/
│       │   ├── LoginPage.vue
│       │   ├── DashboardPage.vue
│       │   ├── ResourceListPage.vue
│       │   └── ResourceDetailPage.vue
│       ├── components/
│       │   ├── ui/                    # 기본 UI 컴포넌트
│       │   │   ├── BaseButton.vue
│       │   │   ├── BaseInput.vue
│       │   │   ├── BaseTable.vue
│       │   │   ├── BasePagination.vue
│       │   │   └── BaseToast.vue
│       │   ├── features/
│       │   │   ├── auth/
│       │   │   │   └── LoginForm.vue
│       │   │   └── resources/
│       │   │       ├── ResourceFilter.vue
│       │   │       └── ResourceForm.vue
│       │   └── layouts/
│       │       ├── AppLayout.vue
│       │       └── AuthLayout.vue
│       └── types/
│           ├── api.types.ts           # 공통 API 타입
│           ├── auth.types.ts
│           └── resource.types.ts
│
└── backend/                           # Spring Boot 3.x
    ├── pom.xml (또는 build.gradle)
    ├── docker-compose.yml
    ├── .env.example
    └── src/
        ├── main/
        │   ├── java/com/example/app/
        │   │   ├── Application.java   # @SpringBootApplication
        │   │   │
        │   │   ├── config/            # 설정 클래스
        │   │   │   ├── SecurityConfig.java
        │   │   │   ├── JwtConfig.java
        │   │   │   ├── WebClientConfig.java
        │   │   │   ├── FeignConfig.java
        │   │   │   ├── RedisConfig.java
        │   │   │   ├── CorsConfig.java
        │   │   │   └── SwaggerConfig.java
        │   │   │
        │   │   ├── common/            # 공통
        │   │   │   ├── response/
        │   │   │   │   ├── ApiResponse.java      # 공통 응답 래퍼
        │   │   │   │   └── PageResponse.java     # 페이지네이션 응답
        │   │   │   ├── exception/
        │   │   │   │   ├── GlobalExceptionHandler.java
        │   │   │   │   ├── BusinessException.java
        │   │   │   │   └── ErrorCode.java        # 에러 코드 열거형
        │   │   │   └── aop/
        │   │   │       └── ExternalApiLoggingAspect.java
        │   │   │
        │   │   ├── domain/
        │   │   │   ├── auth/
        │   │   │   │   ├── controller/
        │   │   │   │   │   └── AuthController.java
        │   │   │   │   ├── service/
        │   │   │   │   │   ├── AuthService.java
        │   │   │   │   │   └── OAuth2UserService.java
        │   │   │   │   ├── dto/
        │   │   │   │   │   ├── LoginRequest.java
        │   │   │   │   │   ├── LoginResponse.java
        │   │   │   │   │   └── TokenRefreshRequest.java
        │   │   │   │   └── entity/
        │   │   │   │       └── User.java
        │   │   │   │
        │   │   │   ├── resource/      # 예시 도메인
        │   │   │   │   ├── controller/
        │   │   │   │   │   └── ResourceController.java
        │   │   │   │   ├── service/
        │   │   │   │   │   └── ResourceService.java
        │   │   │   │   ├── repository/
        │   │   │   │   │   └── ResourceRepository.java
        │   │   │   │   ├── dto/
        │   │   │   │   └── entity/
        │   │   │   │       └── Resource.java
        │   │   │   │
        │   │   │   └── notification/
        │   │   │       ├── service/
        │   │   │       │   └── NotificationService.java
        │   │   │       └── adapter/
        │   │   │           ├── EmailAdapter.java
        │   │   │           └── SmsAdapter.java
        │   │   │
        │   │   └── infrastructure/
        │   │       ├── external/       # 외부 API 연동
        │   │       │   ├── client/
        │   │       │   │   ├── SendGridClient.java     # Feign
        │   │       │   │   ├── SmsClient.java          # Feign
        │   │       │   │   └── ExternalDataClient.java # Feign
        │   │       │   └── fallback/
        │   │       │       └── ExternalDataClientFallback.java
        │   │       └── storage/
        │   │           └── S3StorageService.java
        │   │
        │   └── resources/
        │       ├── application.yml
        │       ├── application-local.yml
        │       ├── application-prod.yml
        │       └── db/migration/      # Flyway
        │           ├── V1__init_schema.sql
        │           ├── V2__add_oauth2_fields.sql
        │           └── V3__add_notification_history.sql
        │
        └── test/
            └── java/com/example/app/
                ├── domain/auth/
                │   └── AuthControllerTest.java
                ├── domain/resource/
                │   └── ResourceServiceTest.java
                └── infrastructure/
                    └── ExternalClientTest.java
```

## 코딩 컨벤션

| 항목 | 규칙 |
|------|------|
| 패키지 구조 | 도메인 기반 (domain/{도메인명}) |
| Controller | `@RestController`, `@RequestMapping("/api/v1/{resource}")` |
| Service | 인터페이스 + 구현체 분리 (`XXXService`, `XXXServiceImpl`) |
| DTO | record 또는 @Getter Lombok 클래스 |
| 예외 | `BusinessException(ErrorCode)` → `GlobalExceptionHandler` |
| 응답 | 항상 `ApiResponse.success(data)` / `ApiResponse.error(errorCode)` |
