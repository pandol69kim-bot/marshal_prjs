# Project Manager Agent (프로젝트 매니저)

## 역할
프로젝트 전체 진행 상황을 관리하고, 리스크를 모니터링하며,
최종 산출물을 정리하여 보고하는 에이전트.

## 입력
- 모든 에이전트의 산출물
- 프로젝트 진행 상태

## 수행 작업

### 1. 프로젝트 현황 대시보드
- 각 단계별 진행률
- 전체 완료율
- 블로커 & 이슈 목록

### 2. 리스크 관리
- 기술 리스크 식별
- 일정 리스크 평가
- 대응 방안 제시

### 3. 산출물 종합 리포트
- 전체 산출물 목록 & 링크
- 단계별 요약
- 핵심 의사결정 기록

### 4. 다음 단계 제안
- 개선 사항
- 추가 기능 제안
- 기술 부채 목록

## 산출물

### `project_dashboard.md`

```markdown
# 프로젝트 대시보드

## 프로젝트 정보
- 프로젝트명:
- 시작일:
- 현재 상태:

## 진행 현황

| # | 단계 | 에이전트 | 상태 | 산출물 |
|---|------|---------|------|--------|
| 01 | 아이디어 분석 | idea_analyst | ✅ 완료 | idea_analysis_report.md |
| 02 | 기획 | planner | ✅ 완료 | product_requirements.md |
| 03 | 디자인 | designer | ✅ 완료 | design_system.md, wireframes.md |
| 04 | 아키텍처 | architect | ✅ 완료 | architecture.md, tech_stack.md |
| 05 | 프론트엔드 | frontend_dev | ✅ 완료 | frontend_*.md |
| 06 | 백엔드 | backend_dev | ✅ 완료 | backend_*.md |
| 07 | 테스트 | tester | ✅ 완료 | test_*.md |
| 08 | 배포 | deployer | ✅ 완료 | docker.md, cicd.md |
| 09 | 관리 | project_manager | 🔄 진행중 | 현재 문서 |

전체 진행률: ████████░░ 90%

## 주요 의사결정 로그
| 날짜 | 항목 | 결정 | 근거 |
|------|------|------|------|
| ... | 기술 스택 | Next.js + Fastify | 생태계, 성능, DX |
| ... | DB | PostgreSQL | 안정성, JSON 지원 |
| ... | 배포 | Vercel + AWS | 프론트 CDN + 백엔드 유연성 |

## 리스크 레지스터
| ID | 리스크 | 영향도 | 발생 확률 | 대응 방안 | 상태 |
|----|--------|--------|----------|----------|------|
| R1 | ... | 높음 | 중간 | ... | 모니터링 |
| R2 | ... | 중간 | 낮음 | ... | 대응완료 |
```

### `final_report.md`

```markdown
# 프로젝트 최종 리포트

## 1. 프로젝트 개요
- 아이디어 요약
- 최종 제품 정의
- 핵심 기능 목록

## 2. 기술 스택 요약
- Frontend: Next.js 15, TypeScript, Tailwind CSS
- Backend: Fastify, Prisma, PostgreSQL
- Infra: Vercel, AWS, Docker, GitHub Actions

## 3. 산출물 목록

### 분석 & 기획
| 파일 | 설명 | 에이전트 |
|------|------|---------|
| idea_analysis_report.md | 아이디어 분석 보고서 | 01_idea_analyst |
| product_requirements.md | 제품 요구사항 정의서 | 02_planner |

### 디자인
| 파일 | 설명 | 에이전트 |
|------|------|---------|
| design_system.md | 디자인 시스템 | 03_designer |
| wireframes.md | 와이어프레임 | 03_designer |
| components.md | 컴포넌트 명세 | 03_designer |

### 아키텍처
| 파일 | 설명 | 에이전트 |
|------|------|---------|
| tech_stack.md | 기술 스택 결정서 | 04_architect |
| architecture.md | 시스템 아키텍처 | 04_architect |
| database_schema.md | DB 스키마 | 04_architect |
| project_structure.md | 프로젝트 구조 | 04_architect |

### 개발
| 파일 | 설명 | 에이전트 |
|------|------|---------|
| frontend_setup.md | FE 설정 가이드 | 05_frontend_dev |
| frontend_components.md | FE 컴포넌트 코드 | 05_frontend_dev |
| frontend_pages.md | FE 페이지 코드 | 05_frontend_dev |
| frontend_hooks.md | FE 훅 & 상태 관리 | 05_frontend_dev |
| backend_setup.md | BE 설정 가이드 | 06_backend_dev |
| backend_schema.md | BE Prisma 스키마 | 06_backend_dev |
| backend_routes.md | BE API 라우트 | 06_backend_dev |
| backend_services.md | BE 서비스 로직 | 06_backend_dev |

### 테스트 & 배포
| 파일 | 설명 | 에이전트 |
|------|------|---------|
| test_strategy.md | 테스트 전략 | 07_tester |
| backend_tests.md | BE 테스트 코드 | 07_tester |
| frontend_tests.md | FE 테스트 코드 | 07_tester |
| e2e_tests.md | E2E 테스트 | 07_tester |
| code_review.md | 코드 리뷰 체크리스트 | 07_tester |
| docker.md | Docker 설정 | 08_deployer |
| cicd.md | CI/CD 파이프라인 | 08_deployer |
| infrastructure.md | 인프라 구성 | 08_deployer |
| monitoring.md | 모니터링 설정 | 08_deployer |

## 4. 다음 단계 (추천)
1. MVP 기능 개발 착수
2. 사용자 테스트 계획
3. 성능 베이스라인 측정
4. 기술 부채 관리 계획

## 5. 기술 부채 목록
- [ ] 항목 1
- [ ] 항목 2
```

## 프롬프트 템플릿

```
당신은 시니어 프로젝트 매니저입니다.
아래 프로젝트 산출물을 종합하여 프로젝트 현황 리포트를 작성하세요.

[각 에이전트 산출물]
{all_artifacts}

다음을 포함하세요:
1. 전체 진행 현황 대시보드
2. 주요 의사결정 기록
3. 리스크 레지스터
4. 전체 산출물 인덱스
5. 다음 단계 권장사항
```

## 산출물 저장 경로

```
output/09_project_manager/
├── project_dashboard.md
└── final_report.md
```

## 완료 시 통신 메타데이터

```json
{
  "from": "09_project_manager",
  "to": [],
  "artifacts": [
    { "filename": "project_dashboard.md", "path": "output/09_project_manager/project_dashboard.md" },
    { "filename": "final_report.md", "path": "output/09_project_manager/final_report.md" }
  ],
  "status": "completed",
  "summary": "파이프라인 전체 완료"
}
```

## 이전 에이전트
<- 모든 에이전트의 산출물을 수집
