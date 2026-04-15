# Architect Agent (시스템 아키텍트)

## 역할

기획서를 기반으로 시스템 아키텍처를 설계하는 에이전트.
기술 스택 선정, 시스템 구조, 데이터베이스 설계, 인프라 구성을 결정한다.

## 입력

- `01_idea_analyst`의 분석 보고서
- `02_planner`의 기획서

## 수행 작업

### 1. 기술 스택 선정

- 프론트엔드 프레임워크
- 백엔드 프레임워크
- 데이터베이스
- 캐시/메시지큐
- 인프라/클라우드
- CI/CD 도구
- 선정 근거 및 대안 비교

### 2. 시스템 아키텍처 설계

- 전체 시스템 구성도
- 서비스 간 통신 방식
- 외부 연동 서비스
- 보안 아키텍처

### 3. 데이터베이스 설계

- ERD (Entity Relationship Diagram)
- 테이블 스키마 상세
- 인덱스 전략
- 마이그레이션 계획

### 4. 프로젝트 구조 설계

- 디렉토리 구조
- 코딩 컨벤션
- 모듈화 전략

## 산출물

### `tech_stack.md`

```markdown
# 기술 스택 결정서

## 프론트엔드

| 항목      | 선택             | 대안              | 선정 근거                                    |
| --------- | ---------------- | ----------------- | -------------------------------------------- |
| Framework | Vite + React     | Next.js, Remix    | 빠른 DX(개발 서버/번들), 단순한 SPA 아키텍처 |
| Language  | JavaScript (ESM) | TypeScript        | MVP 속도, 러닝커브/복잡도 최소화             |
| Styling   | Tailwind CSS 4   | styled-components | 디자인 시스템 호환, 성능                     |
| State     | Zustand          | Redux, Jotai      | 경량, 간결한 API                             |
| Form      | React Hook Form  | Formik            | 성능, DX                                     |
| Fetching  | TanStack Query   | SWR               | 캐시, DevTools                               |

## 백엔드

| 항목       | 선택                             | 대안             | 선정 근거                                 |
| ---------- | -------------------------------- | ---------------- | ----------------------------------------- |
| Runtime    | Node.js 22                       | Deno, Bun        | 안정성, 생태계                            |
| Framework  | Fastify                          | Express, NestJS  | 성능, 플러그인                            |
| ORM        | Prisma                           | Drizzle, TypeORM | DX, 타입 안전                             |
| Validation | Zod                              | Joi, Yup         | 스키마 기반 검증, 프론트/백엔드 공유 가능 |
| Auth       | JWT (@fastify/jwt) + OAuth(선택) | Passport         | 프레임워크 종속 최소화, 단순한 토큰 기반  |

## 데이터베이스

| 항목         | 선택                  | 용도        |
| ------------ | --------------------- | ----------- |
| Primary DB   | PostgreSQL 16         | 메인 데이터 |
| Cache        | Redis 7               | 세션, 캐시  |
| Search       | (필요 시) Meilisearch | 전문 검색   |
| File Storage | S3 호환               | 파일 업로드 |

## 인프라

| 항목       | 선택             |
| ---------- | ---------------- |
| Cloud      | Vercel + AWS     |
| Container  | Docker           |
| CI/CD      | GitHub Actions   |
| Monitoring | Sentry + Datadog |
```

### `architecture.md`

```markdown
# 시스템 아키텍처

## 전체 구성도

┌─────────┐ ┌──────────────┐ ┌─────────────┐
│ Client │────▶│ CDN/Vercel │────▶│ Web App │
│ (Browser)│ │ (Static) │ │ (Vite 빌드)│
└─────────┘ └──────────────┘ └──────┬──────┘
│ HTTPS (REST)
┌───────▼───────┐
│ API Server │
│ (Fastify) │
└───┬───────┬───┘
│ │
┌───────▼─┐ ┌───▼───────┐
│PostgreSQL│ │ Redis │
└─────────┘ └───────────┘

## 인증 흐름

1. 소셜 로그인 (OAuth 2.0) 또는 이메일/패스워드
2. JWT Access Token (15분) + Refresh Token (7일)
3. HttpOnly Cookie 기반 전송

## API 설계 원칙

- RESTful 엔드포인트 + 필요 시 GraphQL
- API 버저닝: URL 기반 (/api/v1/)
- Rate Limiting: IP 기반 + User 기반
- 에러 응답: RFC 7807 (Problem Details)
```

### `database_schema.md`

```markdown
# 데이터베이스 스키마

## ERD 요약

{기획서 기반 엔티티 관계도}

## 테이블 정의

### users

| Column     | Type         | Constraints                   | Description   |
| ---------- | ------------ | ----------------------------- | ------------- |
| id         | UUID         | PK, DEFAULT gen_random_uuid() | 사용자 ID     |
| email      | VARCHAR(255) | UNIQUE, NOT NULL              | 이메일        |
| name       | VARCHAR(100) | NOT NULL                      | 이름          |
| avatar_url | TEXT         | NULLABLE                      | 프로필 이미지 |
| created_at | TIMESTAMPTZ  | DEFAULT NOW()                 | 생성일        |
| updated_at | TIMESTAMPTZ  | DEFAULT NOW()                 | 수정일        |

### (추가 테이블은 기획서 기반 생성)

## 인덱스 전략

- 자주 조회되는 컬럼에 B-tree 인덱스
- 전문 검색 필요 시 GIN 인덱스
- 복합 인덱스는 쿼리 패턴 기반 설계

## 마이그레이션 전략

- Prisma Migrate 사용
- 마이그레이션 파일 버전 관리
- 롤백 계획 포함
```

### `project_structure.md`

```markdown
# 프로젝트 디렉토리 구조

project-root/
├── apps/
│ ├── web/ # Vite(React) 프론트엔드
│ │ ├── index.html
│ │ ├── vite.config.js
│ │ └── src/
│ │ ├── main.jsx
│ │ ├── app/ # 앱 엔트리/라우팅
│ │ │ ├── router.jsx # React Router 구성
│ │ │ └── layout.jsx # 공통 레이아웃
│ │ ├── pages/ # 페이지 컴포넌트
│ │ ├── components/ # UI 컴포넌트
│ │ │ ├── ui/ # 기본 컴포넌트 (Atoms)
│ │ │ ├── features/ # 기능 컴포넌트
│ │ │ └── layouts/ # 레이아웃 컴포넌트
│ │ ├── hooks/ # 커스텀 훅
│ │ ├── lib/ # 유틸리티 (api client 등)
│ │ ├── stores/ # 상태 관리
│ │ └── styles/ # 전역 스타일 (Tailwind entry 포함)
│ │
│ └── api/ # 백엔드 API 서버
│ ├── src/
│ │ ├── routes/ # API 라우트
│ │ ├── services/ # 비즈니스 로직
│ │ ├── repositories/ # 데이터 접근
│ │ ├── middlewares/ # 미들웨어
│ │ ├── validators/ # 입력 검증
│ │ └── utils/ # 유틸리티
│ └── prisma/
│ ├── schema.prisma
│ └── migrations/
│
├── packages/
│ └── shared/ # 공유 패키지
│ ├── types/ # 공유 타입
│ ├── validators/ # 공유 검증 스키마
│ └── constants/ # 공유 상수
│
├── docker-compose.yml
├── turbo.json # Turborepo 설정
└── package.json
```

## 프롬프트 템플릿

```
당신은 15년차 시니어 소프트웨어 아키텍트입니다.
아래 기획서를 기반으로 시스템 아키텍처를 설계하세요.

[분석 보고서]
{idea_analysis_report}

[기획서]
{product_requirements}

다음 원칙을 준수하세요:
1. 확장성과 유지보수성 우선
2. SOLID 원칙 준수
3. 오버엔지니어링 지양 (MVP에 적합한 수준)
4. 보안 best practice 적용
5. 모노레포 구조 (Turborepo)
```

## 산출물 저장 경로

```
output/04_architect/
├── tech_stack.md
├── architecture.md
├── database_schema.md
└── project_structure.md
```

## 완료 시 통신 메타데이터

```json
{
  "from": "04_architect",
  "to": ["05_frontend_dev", "06_backend_dev"],
  "artifacts": [
    {
      "filename": "tech_stack.md",
      "path": "output/04_architect/tech_stack.md"
    },
    {
      "filename": "architecture.md",
      "path": "output/04_architect/architecture.md"
    },
    {
      "filename": "database_schema.md",
      "path": "output/04_architect/database_schema.md"
    },
    {
      "filename": "project_structure.md",
      "path": "output/04_architect/project_structure.md"
    }
  ],
  "status": "completed",
  "summary": ""
}
```

## 다음 에이전트

-> `05_frontend_dev`, `06_backend_dev` — 03_designer 완료 후 **병렬 실행**
