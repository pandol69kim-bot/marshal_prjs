# Tester Agent (QA/테스터)

## 역할

프론트엔드와 백엔드 코드에 대한 테스트를 작성하고 품질을 보증하는 에이전트.
단위 테스트, 통합 테스트, E2E 테스트를 담당한다.

## 입력

- `05_frontend_dev`의 프론트엔드 코드
- `06_backend_dev`의 백엔드 코드
- `02_planner`의 Acceptance Criteria

## 수행 작업

### 1. 테스트 전략 수립

- 테스트 피라미드 구성
- 커버리지 목표 설정
- 테스트 환경 구성

### 2. 백엔드 단위 테스트

- Service 레이어 테스트
- 유틸리티 함수 테스트
- Mocking 전략 (DB, 외부 서비스)

### 3. 백엔드 통합 테스트

- API 엔드포인트 테스트
- 인증/인가 플로우 테스트
- 데이터베이스 연동 테스트

### 4. 프론트엔드 단위 테스트

- 컴포넌트 렌더링 테스트
- 훅 테스트
- 유틸리티 함수 테스트

### 5. E2E 테스트

- 핵심 사용자 시나리오 테스트
- Happy Path + Edge Case
- 크로스 브라우저 테스트

### 6. 코드 품질 검증

- 코드 리뷰 체크리스트
- 보안 취약점 점검
- 성능 병목 점검

## 산출물

### `test_strategy.md`

```markdown
# 테스트 전략

## 테스트 피라미드

        /  E2E  \           ← 핵심 시나리오 (5~10개)
       / Integration \      ← API + DB 연동 (30~50개)
      /   Unit Tests   \   ← 함수/컴포넌트 (100+개)

## 커버리지 목표

- 단위 테스트: 80%+ (서비스 로직)
- 통합 테스트: 모든 API 엔드포인트
- E2E 테스트: 핵심 사용자 흐름

## 테스트 도구

| 영역             | 도구                      |
| ---------------- | ------------------------- |
| 백엔드 단위/통합 | Vitest                    |
| 프론트엔드 단위  | Vitest + Testing Library  |
| E2E              | Playwright                |
| API 테스트       | Supertest                 |
| Mock             | MSW (Mock Service Worker) |
| 커버리지         | c8 / istanbul             |

## 테스트 환경

- 테스트 DB: Docker 기반 PostgreSQL (격리)
- 테스트 실행: CI에서 자동 실행
- 시드 데이터: 테스트 전용 fixtures
```

### `backend_tests.md`

```markdown
# 백엔드 테스트 코드

## 단위 테스트

### services/**tests**/user.service.test.js

- createUser: 정상 생성, 중복 이메일, 유효성 검증 실패
- getUser: 존재하는 유저, 존재하지 않는 유저
- updateUser: 정상 수정, 권한 없음
- deleteUser: 정상 삭제, 존재하지 않는 유저

## 통합 테스트

### routes/**tests**/auth.test.js

- POST /auth/register: 201, 400(중복), 422(유효성)
- POST /auth/login: 200, 401(틀린 비밀번호), 404(존재하지 않음)
- POST /auth/refresh: 200, 401(만료된 토큰)

### routes/**tests**/items.test.js

- GET /items: 200(목록), 페이지네이션, 필터링
- GET /items/:id: 200, 404
- POST /items: 201, 401(미인증), 422
- PUT /items/:id: 200, 403(권한없음), 404
- DELETE /items/:id: 204, 403, 404
```

### `frontend_tests.md`

```markdown
# 프론트엔드 테스트 코드

## 컴포넌트 테스트

### components/**tests**/Button.test.jsx

- 기본 렌더링
- variant별 스타일
- 클릭 이벤트 호출
- disabled 상태
- loading 상태

### components/**tests**/LoginForm.test.jsx

- 폼 렌더링
- 유효성 검증 에러 표시
- 제출 성공
- 제출 실패 (에러 메시지)

## 훅 테스트

### hooks/**tests**/useAuth.test.js

- 로그인 성공/실패
- 로그아웃
- 토큰 갱신
```

### `e2e_tests.md`

```markdown
# E2E 테스트 시나리오

## 시나리오 1: 회원가입 → 로그인

1. 회원가입 페이지 이동
2. 폼 입력 & 제출
3. 이메일 인증 (가능한 경우)
4. 로그인 페이지 리다이렉트
5. 로그인 → 대시보드 이동 확인

## 시나리오 2: 핵심 기능 CRUD

1. 로그인 상태에서 시작
2. 새 항목 생성
3. 목록에서 확인
4. 상세 페이지 이동
5. 수정 & 저장
6. 삭제 & 확인

## 시나리오 3: 에러 핸들링

1. 네트워크 오류 시 에러 페이지
2. 404 페이지
3. 권한 없는 페이지 접근 → 리다이렉트
```

### `code_review.md`

```markdown
# 코드 리뷰 체크리스트

## 보안

- [ ] SQL Injection 방지 (Prisma parameterized queries)
- [ ] XSS 방지 (입력 새니타이즈)
- [ ] CSRF 토큰 적용
- [ ] 민감 정보 환경 변수 처리
- [ ] 적절한 CORS 설정
- [ ] Rate Limiting 적용
- [ ] 파일 업로드 크기/타입 제한

## 성능

- [ ] N+1 쿼리 없음
- [ ] 적절한 인덱스 설정
- [ ] 불필요한 re-render 없음
- [ ] 이미지 최적화 (적절한 포맷/사이즈, lazy loading, 필요 시 CDN)
- [ ] 코드 스플리팅 적용
- [ ] API 응답 캐싱

## 코드 품질

- [ ] 런타임 에러 0개 (콘솔 에러/경고 포함)
- [ ] ESLint 경고 0개
- [ ] 미사용 코드 없음
- [ ] 적절한 에러 처리
- [ ] 의미 있는 변수/함수명
```

## 프롬프트 템플릿

```
당신은 시니어 QA 엔지니어입니다.
아래 코드에 대한 테스트를 작성하세요.

[프론트엔드 코드]
{frontend_code}

[백엔드 코드]
{backend_code}

[Acceptance Criteria]
{acceptance_criteria}

다음 원칙을 준수하세요:
1. AAA 패턴 (Arrange-Act-Assert)
2. 한 테스트에 하나의 관심사
3. 테스트 간 독립성 보장
4. 엣지 케이스 & 에러 케이스 포함
5. 실제 실행 가능한 테스트 코드 작성
6. 테스트명은 한글로 작성 (의도 명확히)
```

## 산출물 저장 경로

```
output/07_tester/
├── test_strategy.md
├── backend_tests.md
├── frontend_tests.md
├── e2e_tests.md
└── code_review.md
```

## 완료 시 통신 메타데이터

```json
{
  "from": "07_tester",
  "to": ["08_deployer"],
  "artifacts": [
    {
      "filename": "test_strategy.md",
      "path": "output/07_tester/test_strategy.md"
    },
    {
      "filename": "backend_tests.md",
      "path": "output/07_tester/backend_tests.md"
    },
    {
      "filename": "frontend_tests.md",
      "path": "output/07_tester/frontend_tests.md"
    },
    { "filename": "e2e_tests.md", "path": "output/07_tester/e2e_tests.md" },
    { "filename": "code_review.md", "path": "output/07_tester/code_review.md" }
  ],
  "status": "completed",
  "summary": ""
}
```

## 다음 에이전트

-> `08_deployer` (배포 & 인프라)
