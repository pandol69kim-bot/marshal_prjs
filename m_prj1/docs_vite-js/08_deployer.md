# Deployer Agent (배포/인프라 엔지니어)

## 역할
프로젝트의 배포 환경을 구성하고, CI/CD 파이프라인을 설정하며,
모니터링 및 운영 환경을 셋업하는 에이전트.

## 입력
- `04_architect`의 아키텍처 & 인프라 설계
- `05_frontend_dev`, `06_backend_dev`의 코드
- `07_tester`의 테스트 코드

## 수행 작업

### 1. 컨테이너화 (Docker)
- 프론트엔드 Dockerfile
- 백엔드 Dockerfile
- docker-compose (개발/운영)
- 멀티 스테이지 빌드 최적화

### 2. CI/CD 파이프라인
- GitHub Actions 워크플로우
- 테스트 자동 실행
- 빌드 & 배포 자동화
- 환경별 배포 (staging/production)

### 3. 인프라 구성
- 클라우드 리소스 설정
- 도메인 & SSL 설정
- 환경 변수 관리 (Secrets)
- 스케일링 정책

### 4. 모니터링 & 로깅
- 에러 트래킹 (Sentry)
- 성능 모니터링
- 로그 수집
- 알림 설정

### 5. 보안 & 백업
- 보안 헤더 설정
- 백업 전략
- 재해 복구 계획

## 산출물

### `docker.md`

```markdown
# Docker 설정

## Dockerfile (Backend)
FROM node:22-alpine AS base
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

FROM base AS build
RUN npm ci
COPY . .
RUN npm run build

FROM base AS production
COPY --from=build /app/dist ./dist
COPY --from=build /app/prisma ./prisma
RUN npx prisma generate
EXPOSE 3001
CMD ["node", "dist/server.js"]

## Dockerfile (Frontend)
FROM node:22-alpine AS base
WORKDIR /app

FROM base AS deps
COPY package*.json ./
RUN npm ci

FROM base AS build
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM base AS production
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
COPY --from=build /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]

## docker-compose.prod.yml
services:
  web:
    build: ./apps/web
    ports: ["3000:3000"]
    environment:
      - NEXT_PUBLIC_API_URL=https://api.example.com

  api:
    build: ./apps/api
    ports: ["3001:3001"]
    environment:
      - DATABASE_URL=postgresql://...
      - REDIS_URL=redis://redis:6379
    depends_on: [postgres, redis]

  postgres:
    image: postgres:16-alpine
    volumes: [pgdata:/var/lib/postgresql/data]

  redis:
    image: redis:7-alpine
```

### `cicd.md`

```markdown
# CI/CD 파이프라인

## .github/workflows/ci.yml

name: CI
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: 22 }
      - run: npm ci
      - run: npm run lint
      - run: npm run type-check

  test:
    runs-on: ubuntu-latest
    needs: lint
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports: ["5432:5432"]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
      - run: npm ci
      - run: npx prisma migrate deploy
      - run: npm run test -- --coverage
      - uses: codecov/codecov-action@v4

  build:
    runs-on: ubuntu-latest
    needs: test
    steps:
      - uses: actions/checkout@v4
      - run: npm ci
      - run: npm run build

  deploy-staging:
    if: github.ref == 'refs/heads/develop'
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to Staging
        run: echo "Deploy to staging environment"

  deploy-production:
    if: github.ref == 'refs/heads/main'
    needs: build
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Deploy to Production
        run: echo "Deploy to production environment"
```

### `infrastructure.md`

```markdown
# 인프라 구성

## 아키텍처 (Vercel + AWS)

┌──────────┐    ┌──────────┐    ┌──────────────┐
│ Cloudflare│───▶│  Vercel  │    │  AWS (VPC)   │
│   (DNS)  │    │ (Frontend)│    │              │
└──────────┘    └──────────┘    │ ┌──────────┐ │
                                │ │ ECS/EC2  │ │
                    ┌───────────┤ │  (API)   │ │
                    │           │ └────┬─────┘ │
                    │           │      │       │
                    │           │ ┌────▼─────┐ │
                    │           │ │   RDS    │ │
                    │           │ │(Postgres)│ │
                    │           │ └──────────┘ │
                    │           │ ┌──────────┐ │
                    │           │ │ElastiCache│ │
                    │           │ │ (Redis)  │ │
                    │           │ └──────────┘ │
                    │           └──────────────┘

## 환경 구성
| 환경 | 용도 | URL |
|------|------|-----|
| Development | 로컬 개발 | localhost:3000 |
| Staging | QA/테스트 | staging.example.com |
| Production | 서비스 운영 | www.example.com |

## 스케일링 정책
- Frontend: Vercel 자동 스케일링
- Backend: CPU 70% 초과 시 Auto Scaling
- DB: Read Replica 구성 (트래픽 증가 시)

## 백업 정책
- DB: 일일 자동 백업 (30일 보관)
- 파일: S3 버전닝 활성화
- 설정: Infrastructure as Code (Terraform)
```

### `monitoring.md`

```markdown
# 모니터링 & 알림

## Sentry 설정
- 프론트엔드: @sentry/nextjs
- 백엔드: @sentry/node
- Source Maps 업로드
- 에러 그룹핑 & 알림

## 헬스체크
- GET /health → { status: "ok", version, uptime }
- DB 연결 확인
- Redis 연결 확인

## 알림 규칙
| 조건 | 채널 | 심각도 |
|------|------|--------|
| 5xx 에러 5회/분 | Slack #alerts | Critical |
| API 응답 > 3초 | Slack #alerts | Warning |
| CPU > 80% 5분 | Slack #infra | Warning |
| DB 연결 실패 | Slack #alerts + PagerDuty | Critical |
| 디스크 > 90% | Slack #infra | Warning |

## 대시보드
- 요청 수 / 에러율 / 응답시간
- CPU / 메모리 / 디스크
- DB 커넥션 풀 / 쿼리 성능
- 사용자 활성 세션 수
```

## 프롬프트 템플릿

```
당신은 시니어 DevOps/인프라 엔지니어입니다.
아래 프로젝트에 대한 배포 환경을 구성하세요.

[아키텍처]
{architecture}

[기술 스택]
{tech_stack}

다음 원칙을 준수하세요:
1. 컨테이너 이미지 최소화 (멀티 스테이지)
2. 환경 변수로 설정 분리
3. CI에서 테스트 필수 통과
4. Zero-downtime 배포
5. 적절한 모니터링 & 알림
6. 보안 best practice (최소 권한 원칙)
```

## 산출물 저장 경로

```
output/08_deployer/
├── docker.md
├── cicd.md
├── infrastructure.md
└── monitoring.md
```

## 완료 시 통신 메타데이터

```json
{
  "from": "08_deployer",
  "to": ["09_project_manager"],
  "artifacts": [
    { "filename": "docker.md", "path": "output/08_deployer/docker.md" },
    { "filename": "cicd.md", "path": "output/08_deployer/cicd.md" },
    { "filename": "infrastructure.md", "path": "output/08_deployer/infrastructure.md" },
    { "filename": "monitoring.md", "path": "output/08_deployer/monitoring.md" }
  ],
  "status": "completed",
  "summary": ""
}
```

## 다음 에이전트
-> `09_project_manager` (프로젝트 관리 & 최종 리포트)
