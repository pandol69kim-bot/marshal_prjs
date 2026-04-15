# 아이디어 분석 보고서

## 1. 핵심 아이디어 요약

**주제**: 외부 시스템과의 연동 및 REST API 설계/개발을 중심으로 한 웹 서비스  
**기술 스택**: Vue 3 (Vite) + Spring Boot  
**핵심 가치**: 다양한 외부 시스템(공공 API, 결제, 알림, 인증 등)과의 안정적 연동 + 표준화된 REST API 제공

---

## 2. 문제 정의

| 구분 | 내용 |
|------|------|
| 문제 | 외부 시스템 연동 시 표준화되지 않은 방식으로 인한 유지보수 어려움 |
| 원인 | API 설계 일관성 부재, 에러 처리 미흡, 외부 의존성 캡슐화 부족 |
| 해결 | 표준 REST API 설계 + Feign/WebClient 기반 외부 연동 추상화 |

---

## 3. 타겟 사용자

| 페르소나 | 설명 |
|---------|------|
| 백엔드 개발자 | Spring Boot 기반 REST API 서버를 구축하고 외부 API 연동이 필요한 개발자 |
| 프론트엔드 개발자 | Vue 3로 API를 소비하는 SPA를 개발하는 개발자 |
| 시스템 통합 담당자 | 다수의 외부 서비스(결제, 알림, 소셜 로그인 등)와 연동이 필요한 팀 |

---

## 4. MVP 핵심 기능

### 우선순위 High
- JWT 기반 인증/인가 (Spring Security + OAuth2)
- 표준 REST API 설계 (버저닝, 공통 응답 포맷, 에러 핸들링)
- 외부 REST API 연동 레이어 (WebClient / Feign Client)
- Vue 3 SPA - API 연동 패턴 (axios interceptor, composable)

### 우선순위 Medium
- 외부 알림 연동 (이메일/SMS/Slack webhook)
- 파일 업로드 (S3 호환 스토리지)
- 페이지네이션 + 정렬 + 필터링 API

### 우선순위 Low
- 비동기 처리 (Spring @Async + 상태 조회 API)
- API Rate Limiting & Circuit Breaker (Resilience4j)
- WebSocket 실시간 연동

---

## 5. 기술적 기회/도전

| 항목 | 내용 |
|------|------|
| 기회 | Spring Boot 3.x + Virtual Thread (JDK 21) 성능 향상 |
| 도전 | 외부 API 장애 시 Circuit Breaker 패턴 적용 |
| 도전 | OAuth2 소셜 로그인 다중 Provider 처리 |
| 기회 | Vue 3 Composition API + composable로 API 로직 재사용성 극대화 |

---

## 6. 경쟁 분석

| 패턴 | 장점 | 단점 |
|------|------|------|
| RestTemplate (레거시) | 간단함 | 동기식, deprecated 예정 |
| WebClient | 비동기/반응형, 현대적 | 학습 곡선 |
| Feign Client | 선언적, 간결 | 동기식 (기본) |
| **WebClient + Feign 혼용** | 각 상황에 최적 | 복잡성 |

---

## 7. 성공 지표 (KPI)

- API 응답 시간 P95 < 200ms
- 외부 연동 실패율 < 0.1%
- 코드 커버리지 80% 이상
- API 문서화 (Swagger/OpenAPI 3.0) 100%
