# 기술 스택 결정서 (Vue 3 + Spring Boot)

## 프론트엔드

| 항목 | 선택 | 대안 | 선정 근거 |
|------|------|------|---------|
| Build Tool | Vite 5 | Webpack, Parcel | 빠른 HMR, 경량 설정 |
| Framework | Vue 3 (Composition API) | React, Nuxt.js | 한국 생태계 친숙도, 직관적 템플릿 |
| Language | TypeScript | JavaScript | 타입 안전성, IDE 지원 |
| Styling | Tailwind CSS 3 | Element Plus, Vuetify | 유틸리티 우선, 커스터마이징 |
| State | Pinia | Vuex 4 | Vue 3 공식 권장, DevTools 지원 |
| Router | Vue Router 4 | - | Vue 공식 라우터 |
| HTTP Client | Axios | Fetch API, ky | Interceptor, 브라우저 호환성 |
| Server State | TanStack Query (Vue) | VueQuery | 캐싱, 무효화, DevTools |
| Form | VeeValidate 4 + Zod | FormKit | Composition API 친화적 |
| Testing | Vitest + Vue Test Utils | Jest | Vite 네이티브, 빠른 실행 |

## 백엔드

| 항목 | 선택 | 대안 | 선정 근거 |
|------|------|------|---------|
| Runtime | JDK 21 (Virtual Thread) | JDK 17 | 고성능 I/O 처리, 최신 LTS |
| Framework | Spring Boot 3.3 | Quarkus, Micronaut | 생태계, 레퍼런스 풍부 |
| API 스타일 | Spring MVC (WebFlux 선택적) | JAX-RS | 동기 API 표준, 학습 비용 낮음 |
| ORM | Spring Data JPA + Hibernate | MyBatis, Exposed | 표준, 관계 매핑, 마이그레이션 |
| 마이그레이션 | Flyway | Liquibase | 간결한 SQL 기반 마이그레이션 |
| 검증 | Spring Validation (Jakarta) | Hibernate Validator 직접 | 표준, 어노테이션 기반 |
| 외부 연동 | OpenFeign (Spring Cloud) | WebClient, RestTemplate | 선언적, 가독성 |
| 외부 연동(비동기) | WebClient (Project Reactor) | RestTemplate | 비동기, 스트리밍 |
| 인증/인가 | Spring Security 6 + JWT | custom filter | 표준, OAuth2 통합 |
| Circuit Breaker | Resilience4j | Hystrix (deprecated) | Spring Boot 3.x 공식 지원 |
| API 문서 | SpringDoc OpenAPI 3 | Springfox | Spring Boot 3 지원 |
| 캐시 | Spring Cache + Redis | Caffeine | 분산 캐시, 세션 저장 |

## 데이터베이스

| 항목 | 선택 | 용도 |
|------|------|------|
| Primary DB | PostgreSQL 16 | 메인 데이터 |
| Cache / Session | Redis 7 | JWT 블랙리스트, 캐시 |
| File Storage | MinIO (로컬) / AWS S3 | 파일 업로드 |

## 인프라

| 항목 | 선택 |
|------|------|
| Container | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| 모니터링 | Spring Actuator + Prometheus + Grafana |
| 로그 | Logback + ELK Stack (선택) |
