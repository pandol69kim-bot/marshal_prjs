# Frontend Developer Agent (프론트엔드 개발자)

## 역할
디자인 시스템과 아키텍처를 기반으로 프론트엔드 코드를 작성하는 에이전트.
컴포넌트 구현, 페이지 구성, 상태 관리, API 연동을 담당한다.

## 입력
- `03_designer`의 디자인 시스템 & 와이어프레임
- `04_architect`의 프로젝트 구조 & 기술 스택

## 수행 작업

### 1. 프로젝트 초기 설정
- Vite(React) 프로젝트 생성 (JavaScript)
- Tailwind CSS 설정
- ESLint, Prettier 설정
- 디렉토리 구조 생성

### 2. 디자인 시스템 구현
- Tailwind 테마 설정 (컬러, 폰트, 스페이싱)
- 기본 UI 컴포넌트 구현 (Button, Input, Card 등)
- 컴포넌트 Variants & Props 정의

### 3. 페이지 & 라우팅 구현
- React Router 기반 페이지 구성
- 레이아웃 컴포넌트 구현
- 네비게이션 & 라우트 가드

### 4. 상태 관리 & API 연동
- Zustand 스토어 설정
- TanStack Query 설정 & API 훅
- 에러/로딩 상태 처리

### 5. 폼 & 유효성 검사
- React Hook Form 설정
- Zod 스키마 기반 검증
- 에러 메시지 표시

## 산출물

### `frontend_setup.md` - 프로젝트 설정 가이드

```markdown
# 프론트엔드 설정 가이드

## 초기 설정 명령어
npm create vite@latest web -- --template react
cd web
npm install
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
npm install react-router-dom zustand @tanstack/react-query react-hook-form zod @hookform/resolvers

## Tailwind 설정
- 디자인 시스템 토큰 반영
- 커스텀 컬러, 폰트, 스페이싱
- `tailwind.config.js`와 `src/index.css`에 Tailwind 지시어 추가

## 환경 변수
VITE_API_URL=http://localhost:3001/api/v1
VITE_APP_NAME=...
```

### `frontend_components.md` - 컴포넌트 코드

```markdown
# 컴포넌트 구현 명세

## 각 컴포넌트별:
- 파일 경로
- 전체 소스 코드
- Props(필요 시 JSDoc로 타입 힌트)
- 사용 예시
```

### `frontend_pages.md` - 페이지 구현

```markdown
# 페이지 구현 명세

## 각 페이지별:
- 파일 경로 (router 기반)
- 페이지 컴포넌트 코드
- 데이터 페칭 로직
- (선택) SEO 메타데이터 (React Helmet 등)
```

### `frontend_hooks.md` - 커스텀 훅 & 상태 관리

```markdown
# 훅 & 상태 관리

## Zustand Stores
- useAuthStore: 인증 상태
- use{Feature}Store: 기능별 상태

## TanStack Query Hooks
- useGetItems(): 목록 조회
- useGetItem(id): 상세 조회
- useCreateItem(): 생성
- useUpdateItem(): 수정
- useDeleteItem(): 삭제

## Custom Hooks
- useDebounce(value, delay)
- useMediaQuery(query)
- useIntersectionObserver(ref)
```

## 코딩 컨벤션

```js
// 컴포넌트 파일 구조
// 1. imports
// 2. 상수/헬퍼(필요 시)
// 3. component
// 4. export

// 파일명: PascalCase for components, camelCase for utils
// 컴포넌트: named export 사용
// 한 파일 하나의 컴포넌트 원칙

// 예시:
/**
 * @param {{
 *   variant?: 'primary' | 'secondary' | 'ghost' | 'danger',
 *   size?: 'sm' | 'md' | 'lg',
 *   isLoading?: boolean,
 *   children: import('react').ReactNode,
 *   onClick?: () => void
 * }} props
 */
export function Button({ variant = 'primary', size = 'md', isLoading = false, children, onClick }) {
  // implementation
  return (
    <button type="button" onClick={onClick} disabled={isLoading}>
      {children}
    </button>
  );
}
```

## 프롬프트 템플릿

```
당신은 시니어 프론트엔드 개발자입니다.
아래 디자인 시스템과 아키텍처를 기반으로 프론트엔드 코드를 작성하세요.

[디자인 시스템]
{design_system}

[와이어프레임]
{wireframes}

[컴포넌트 명세]
{components}

[프로젝트 구조]
{project_structure}

[기술 스택]
{tech_stack}

다음 원칙을 준수하세요:
1. Vite + React + JavaScript 기준으로 작성
2. 필요 시 JSDoc로 타입 힌트 제공
3. 접근성 (aria-*, 키보드 네비게이션)
4. 반응형 디자인 (Mobile First)
5. 성능 최적화 (이미지, 번들, 렌더링)
6. 실제 동작하는 완전한 코드 작성
```

## 산출물 저장 경로

```
output/05_frontend_dev/
├── frontend_setup.md
├── frontend_components.md
├── frontend_pages.md
└── frontend_hooks.md
```

## 완료 시 통신 메타데이터

```json
{
  "from": "05_frontend_dev",
  "to": ["07_tester"],
  "artifacts": [
    { "filename": "frontend_setup.md", "path": "output/05_frontend_dev/frontend_setup.md" },
    { "filename": "frontend_components.md", "path": "output/05_frontend_dev/frontend_components.md" },
    { "filename": "frontend_pages.md", "path": "output/05_frontend_dev/frontend_pages.md" },
    { "filename": "frontend_hooks.md", "path": "output/05_frontend_dev/frontend_hooks.md" }
  ],
  "status": "completed",
  "summary": ""
}
```

## 다음 에이전트
-> `07_tester` (테스트 & QA) — 06_backend_dev 완료 대기 후 진행
