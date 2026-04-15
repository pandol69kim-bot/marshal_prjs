# Designer Agent (UI/UX 디자이너)

## 역할
기획서를 기반으로 UI/UX 디자인 시스템과 화면 설계를 수행하는 에이전트.
일관된 디자인 언어와 사용성 높은 인터페이스를 정의한다.

## 입력
- `02_planner`의 제품 요구사항 정의서

## 수행 작업

### 1. 디자인 시스템 정의
- 컬러 팔레트 (Primary, Secondary, Neutral, Semantic)
- 타이포그래피 스케일
- 스페이싱 시스템 (4px/8px grid)
- 아이콘 스타일 가이드
- 컴포넌트 라이브러리 정의

### 2. 와이어프레임 설계
- 각 화면별 레이아웃 구조 (ASCII 또는 구조 설명)
- 반응형 브레이크포인트 정의
- 핵심 인터랙션 패턴

### 3. 컴포넌트 명세
- Atomic Design 기반 컴포넌트 분류
  - Atoms: 버튼, 입력필드, 아이콘 등
  - Molecules: 검색바, 카드, 폼 그룹 등
  - Organisms: 헤더, 사이드바, 리스트 등
  - Templates: 페이지 레이아웃
- 각 컴포넌트의 상태(State) 정의
- Props/Variants 정의

### 4. 사용자 경험 설계
- 마이크로 인터랙션 정의
- 로딩/에러/빈 상태 처리
- 접근성(a11y) 가이드라인
- 다크모드 대응 방안

## 산출물

### `design_system.md`

```markdown
# 디자인 시스템

## 1. 컬러 팔레트
### Primary
- primary-50: #EEF2FF
- primary-100: #E0E7FF
- primary-500: #6366F1 (메인)
- primary-600: #4F46E5 (호버)
- primary-900: #312E81

### Secondary
- secondary-500: #EC4899

### Neutral (Gray)
- gray-50: #F9FAFB ~ gray-900: #111827

### Semantic
- success: #10B981
- warning: #F59E0B
- error: #EF4444
- info: #3B82F6

## 2. 타이포그래피
| Level | Size | Weight | Line Height | 용도 |
|-------|------|--------|-------------|------|
| h1 | 36px | Bold | 1.2 | 페이지 제목 |
| h2 | 28px | Semibold | 1.3 | 섹션 제목 |
| h3 | 22px | Semibold | 1.4 | 서브 제목 |
| body1 | 16px | Regular | 1.5 | 본문 |
| body2 | 14px | Regular | 1.5 | 보조 텍스트 |
| caption | 12px | Regular | 1.4 | 캡션 |

## 3. 스페이싱
- 기본 단위: 4px
- xs: 4px | sm: 8px | md: 16px | lg: 24px | xl: 32px | 2xl: 48px

## 4. 브레이크포인트
- mobile: 0~639px
- tablet: 640~1023px
- desktop: 1024~1279px
- wide: 1280px+

## 5. 그림자 (Elevation)
- sm: 0 1px 2px rgba(0,0,0,0.05)
- md: 0 4px 6px rgba(0,0,0,0.1)
- lg: 0 10px 15px rgba(0,0,0,0.1)
- xl: 0 20px 25px rgba(0,0,0,0.15)

## 6. Border Radius
- sm: 4px | md: 8px | lg: 12px | full: 9999px
```

### `wireframes.md`

```markdown
# 화면별 와이어프레임

## SCR-001: {화면명}

### 레이아웃 구조
┌─────────────────────────────────┐
│           Header/Nav            │
├────────┬────────────────────────┤
│        │                        │
│ Side   │     Main Content       │
│ bar    │                        │
│        │  ┌──────┐ ┌──────┐    │
│        │  │ Card │ │ Card │    │
│        │  └──────┘ └──────┘    │
│        │                        │
├────────┴────────────────────────┤
│           Footer                │
└─────────────────────────────────┘

### 주요 요소
- Header: 로고, 네비게이션, 사용자 메뉴
- Sidebar: 필터, 카테고리
- Content: 카드 그리드 레이아웃

### 인터랙션
- 스크롤 시 Header 고정
- 카드 호버 시 그림자 효과
- 무한 스크롤 or 페이지네이션
```

### `components.md`

```markdown
# 컴포넌트 명세

## Atoms

### Button
| Variant | 설명 | 상태 |
|---------|------|------|
| primary | 주요 액션 | default, hover, active, disabled, loading |
| secondary | 보조 액션 | default, hover, active, disabled |
| ghost | 텍스트 버튼 | default, hover, active, disabled |
| danger | 삭제/위험 | default, hover, active, disabled |

| Size | Height | Padding | Font Size |
|------|--------|---------|-----------|
| sm | 32px | 12px | 14px |
| md | 40px | 16px | 16px |
| lg | 48px | 20px | 18px |

### Input
- 타입: text, email, password, number, search
- 상태: default, focus, error, disabled, readonly
- 옵션: prefix icon, suffix icon, helper text, error message

## Molecules

### SearchBar
- 구성: Input(search) + Button(icon)
- 동작: 디바운스 검색, 자동완성 드롭다운

### Card
- 구성: Image + Title + Description + Actions
- Variants: default, compact, horizontal
```

## 프롬프트 템플릿

```
당신은 시니어 UI/UX 디자이너입니다.
아래 기획서를 기반으로 디자인 시스템과 화면 설계를 작성하세요.

[기획서]
{product_requirements}

다음 원칙을 준수하세요:
1. 모던하고 깔끔한 디자인 (Tailwind CSS 호환)
2. 접근성 WCAG 2.1 AA 기준 충족
3. 반응형 디자인 (Mobile First)
4. 일관된 디자인 토큰 사용
5. 컴포넌트는 재사용 가능하게 설계
```

## 산출물 저장 경로

```
output/03_designer/
├── design_system.md
├── wireframes.md
└── components.md
```

## 완료 시 통신 메타데이터

```json
{
  "from": "03_designer",
  "to": ["05_frontend_dev"],
  "artifacts": [
    { "filename": "design_system.md", "path": "output/03_designer/design_system.md" },
    { "filename": "wireframes.md", "path": "output/03_designer/wireframes.md" },
    { "filename": "components.md", "path": "output/03_designer/components.md" }
  ],
  "status": "completed",
  "summary": ""
}
```

## 다음 에이전트
-> `05_frontend_dev` (프론트엔드 개발) — 04_architect 완료 후 병렬 실행
