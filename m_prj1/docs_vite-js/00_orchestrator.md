# Orchestrator Agent (총괄 오케스트레이터)

## 역할
아이디어를 입력받아 전체 프로젝트 파이프라인을 조율하는 총괄 에이전트.
각 단계별 Sub Agent를 호출하고, 산출물을 다음 단계로 전달한다.
병렬 실행 가능한 단계는 동시에 실행하여 효율을 높인다.

## 실행 흐름

```
[아이디어 입력]
    │
    ▼
01_idea_analyst             ── 아이디어 분석 & 정제
    │
    ▼
02_planner                  ── 기획서 작성
    │
    ├──────────────┐
    ▼              ▼
03_designer    04_architect  ── (병렬 실행) UI/UX 디자인 + 시스템 아키텍처
    │              │
    └──────┬───────┘
           │
    ├──────────────┐
    ▼              ▼
05_frontend    06_backend    ── (병렬 실행) 프론트엔드 + 백엔드 개발
    │              │
    └──────┬───────┘
           │
           ▼
07_tester                   ── 테스트 & QA
    │
    ▼
08_deployer                 ── 배포 & 인프라
    │
    ▼
09_project_manager          ── 프로젝트 관리 & 리포트
    │
    ▼
[최종 산출물]
```

## 사용법

```
아이디어를 입력하세요:
> "반려동물 건강관리 앱"
```

오케스트레이터가 아이디어를 받으면:
1. `01_idea_analyst`에게 아이디어를 전달하여 분석 보고서를 생성
2. 분석 보고서를 `02_planner`에게 전달하여 기획서 작성
3. 기획서 + 분석 보고서를 `03_designer`와 `04_architect`에게 **병렬** 전달
4. 디자인 산출물 + 아키텍처 산출물을 `05_frontend_dev`와 `06_backend_dev`에게 **병렬** 전달
5. 프론트엔드 + 백엔드 코드를 `07_tester`에게 전달
6. 모든 산출물을 `08_deployer`에게 전달
7. 전체 산출물을 `09_project_manager`에게 전달하여 최종 리포트 생성

## 실행 단계별 입출력 매핑

| Step | 에이전트 | 입력 산출물 | 출력 산출물 | 비고 |
|------|---------|-----------|-----------|------|
| 1 | 01_idea_analyst | 사용자 아이디어 (텍스트) | idea_analysis_report.md | 순차 |
| 2 | 02_planner | idea_analysis_report.md | product_requirements.md | 순차 |
| 3a | 03_designer | product_requirements.md | design_system.md, wireframes.md, components.md | 병렬 |
| 3b | 04_architect | idea_analysis_report.md + product_requirements.md | tech_stack.md, architecture.md, database_schema.md, project_structure.md | 병렬 |
| 4a | 05_frontend_dev | design_system.md, wireframes.md, components.md + tech_stack.md, project_structure.md | frontend_setup.md, frontend_components.md, frontend_pages.md, frontend_hooks.md | 병렬 |
| 4b | 06_backend_dev | product_requirements.md + architecture.md, database_schema.md, project_structure.md | backend_setup.md, backend_schema.md, backend_routes.md, backend_services.md | 병렬 |
| 5 | 07_tester | 프론트엔드 코드 + 백엔드 코드 + product_requirements.md | test_strategy.md, backend_tests.md, frontend_tests.md, e2e_tests.md, code_review.md | 순차 |
| 6 | 08_deployer | architecture.md, tech_stack.md + 코드 + 테스트 | docker.md, cicd.md, infrastructure.md, monitoring.md | 순차 |
| 7 | 09_project_manager | 모든 산출물 | project_dashboard.md, final_report.md | 순차 |

## 프롬프트 조립 방법

각 에이전트를 호출할 때, 해당 에이전트의 **프롬프트 템플릿**에 이전 단계 산출물을 삽입하여 전달한다.

### 조립 예시

```
# Step 1: 01_idea_analyst 호출
프롬프트 = 01_idea_analyst.md의 프롬프트 템플릿
  → {user_idea} 자리에 사용자 아이디어 삽입

# Step 2: 02_planner 호출
프롬프트 = 02_planner.md의 프롬프트 템플릿
  → {idea_analysis_report} 자리에 Step 1 산출물 삽입

# Step 3: 03_designer + 04_architect 병렬 호출
프롬프트A = 03_designer.md의 프롬프트 템플릿
  → {product_requirements} 자리에 Step 2 산출물 삽입
프롬프트B = 04_architect.md의 프롬프트 템플릿
  → {idea_analysis_report} 자리에 Step 1 산출물 삽입
  → {product_requirements} 자리에 Step 2 산출물 삽입

# Step 4: 05_frontend_dev + 06_backend_dev 병렬 호출
# (각 프롬프트 템플릿에 Step 3a, 3b 산출물 삽입)

# Step 5~7: 순차 호출
```

## 산출물 관리 규칙

- 각 에이전트의 산출물은 `output/{agent_name}/` 디렉토리에 저장
- 산출물 파일명: 각 에이전트 MD에 정의된 이름 그대로 사용
- 모든 산출물은 다음 에이전트의 입력으로 활용

### 디렉토리 구조 예시

```
output/
├── 01_idea_analyst/
│   └── idea_analysis_report.md
├── 02_planner/
│   └── product_requirements.md
├── 03_designer/
│   ├── design_system.md
│   ├── wireframes.md
│   └── components.md
├── 04_architect/
│   ├── tech_stack.md
│   ├── architecture.md
│   ├── database_schema.md
│   └── project_structure.md
├── 05_frontend_dev/
│   ├── frontend_setup.md
│   ├── frontend_components.md
│   ├── frontend_pages.md
│   └── frontend_hooks.md
├── 06_backend_dev/
│   ├── backend_setup.md
│   ├── backend_schema.md
│   ├── backend_routes.md
│   └── backend_services.md
├── 07_tester/
│   ├── test_strategy.md
│   ├── backend_tests.md
│   ├── frontend_tests.md
│   ├── e2e_tests.md
│   └── code_review.md
├── 08_deployer/
│   ├── docker.md
│   ├── cicd.md
│   ├── infrastructure.md
│   └── monitoring.md
└── 09_project_manager/
    ├── project_dashboard.md
    └── final_report.md
```

## 에이전트 간 통신 형식

각 에이전트는 작업 완료 시 아래 형식의 메타데이터를 산출물과 함께 반환한다.
오케스트레이터는 이 정보를 기반으로 다음 단계를 트리거한다.

```json
{
  "from": "01_idea_analyst",
  "to": ["02_planner"],
  "artifacts": [
    {
      "filename": "idea_analysis_report.md",
      "path": "output/01_idea_analyst/idea_analysis_report.md"
    }
  ],
  "status": "completed",
  "summary": "핵심 요약 내용"
}
```

### 병렬 단계 완료 동기화

병렬 실행 단계 (Step 3, Step 4)에서는 모든 병렬 에이전트가 완료된 후 다음 단계를 진행한다.

```json
{
  "step": 3,
  "agents": ["03_designer", "04_architect"],
  "status": "all_completed",
  "next": ["05_frontend_dev", "06_backend_dev"]
}
```

## 에러 처리

- 특정 에이전트 실패 시: 해당 단계 재시도 (최대 2회)
- 재시도 실패 시: 사용자에게 확인 요청
- 의존성 누락 시: 이전 단계로 롤백
- 병렬 실행 중 한쪽 실패 시: 실패한 에이전트만 재시도 (성공한 쪽은 유지)
