# CLAUDE.md

이 파일은 Claude Code(claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

## ⛔ 절대 위반 금지 규칙 (모든 작업 전 반드시 확인)

이 규칙들은 **어떠한 상황에서도, 어떠한 파일 종류든, 어떠한 이유로도** 예외 없이 따라야 합니다.

### 1. master 브랜치 직접 커밋 금지

커밋 전 반드시 현재 브랜치를 확인하세요:

```bash
git branch --show-current
```

결과가 `master`이면 **즉시 중단**하고 브랜치를 먼저 생성하세요:

```bash
git fetch upstream master
git checkout -b <branch-name> upstream/master
```

> `.claude/` 설정 파일, README, 한 줄 수정 — 모두 예외 없음. master에는 절대 직접 커밋하지 않습니다.

### 2. 커밋 전 `/qa-pr` 필수 실행

커밋/푸시 전에 반드시 `/qa-pr` 스킬을 실행하세요. QA 판정이 FAIL이면 커밋하지 마세요.

---

## 프로젝트 개요

Pinpoint Web Frontend v3 — [Pinpoint APM](https://github.com/pinpoint-apm/pinpoint)의 웹 UI로, 오픈소스 애플리케이션 성능 모니터링 도구입니다. React 19 모노레포 구조이며, 서버맵 시각화, 스캐터/히트맵 차트, 트랜잭션 추적, 오류 분석, 설정 관리 기능을 제공합니다.

## 명령어

```bash
# 개발 서버 (Vite dev server, 포트 3000, /api/*를 localhost:8080으로 프록시)
yarn dev

# 빌드 (TypeScript 검사 + Vite 프로덕션 빌드)
yarn build

# 린트 (apps/web에 Prettier → ESLint 순서로 실행)
yarn lint

# 전체 워크스페이스 테스트 실행
yarn test

# 특정 패키지 테스트 실행
yarn workspace @pinpoint-fe/ui test

# 단일 테스트 파일 실행
yarn workspace @pinpoint-fe/ui jest path/to/file.test.ts

# Storybook (UI 컴포넌트 개발, 포트 6006)
yarn workspace @pinpoint-fe/ui storybook

# E2E 테스트 (Playwright)
yarn workspace @pinpoint-fe/web test:e2e

# 전체 정리
yarn clean
```

Node >=22.13.1, Yarn 1.22.22 필요.

## 모노레포 구조

```
apps/web/           → @pinpoint-fe/web     메인 앱 (얇은 페이지 래퍼 + 라우팅)
packages/ui/        → @pinpoint-fe/ui      핵심 라이브러리: 컴포넌트, 훅, 아톰, 상수, 로더
packages/scatter-chart/ → @pinpoint-fe/scatter-chart  캔버스 기반 스캐터 차트 (코어에 React 의존 없음)
packages/server-map/    → @pinpoint-fe/server-map     Cytoscape 기반 네트워크 토폴로지 그래프
packages/datetime-picker/ → @pinpoint-fe/datetime-picker  날짜/시간 선택 컴포넌트
```

Yarn 워크스페이스로 패키지를 연결합니다. 내부 의존성은 `"*"` 버전을 사용합니다.

## 아키텍처

### 페이지 → 컴포넌트 → 훅 패턴

`apps/web/src/pages/`의 페이지는 얇은 래퍼입니다. 거의 모든 로직은 `packages/ui/`에 있습니다:

```
apps/web/src/pages/Inspector.tsx  →  @pinpoint-fe/ui의 InspectorPage를 감싸고
                                     configurationAtom에서 설정을 읽음
```

**중첩 레이아웃 라우트** (`apps/web/src/components/Layout/`): 라우팅은 React Router의 중첩 `<Outlet />` 컴포넌트를 사용하여 공통 관심사를 처리합니다:

- `SideNavigationOutlet` — 사이드 네비게이션 레이아웃으로 페이지를 감쌈
- `InitialFetchOutlet` — 설정 조회, URL 검색 파라미터를 Jotai 아톰에 동기화, 오류 시 `/apiCheck`로 리다이렉트
- `ConfigurationOutlet` — 설정 페이지를 설정 레이아웃으로 감쌈

**라우트 로더** (`packages/ui/src/loader/`): React Router 로더가 렌더링 전에 URL 날짜 파라미터(from/to)를 검증/정규화합니다. 잘못된 날짜 형식이면 올바른 형식으로 리다이렉트합니다.

### 상태 관리

- **Jotai** 아톰 (`packages/ui/src/atoms/`) — 검색 파라미터, 서버맵 데이터, 스캐터 데이터, 설정, 트랜잭션, 토스트 등의 전역 상태
- **React Query** (`@tanstack/react-query`) — 모든 서버 데이터 페칭. `packages/ui/src/hooks/api/`의 커스텀 훅은 `useGetXxx` 패턴을 따름: `END_POINTS`의 엔드포인트와 공유 `queryFn`으로 `useQuery`를 감쌈
- URL 검색 파라미터(`from`, `to`, application)가 시간 범위와 선택된 애플리케이션의 진실의 원천(source of truth)이며, `InitialFetchOutlet`을 통해 아톰에 동기화됨

### API 레이어

- 엔드포인트는 `packages/ui/src/constants/EndPoints.ts`에 정의 — 모두 `/api` 접두사
- 공유 `queryFn`은 `packages/ui/src/hooks/api/reactQueryHelper.ts`에 있으며, 네이티브 `fetch`를 사용하고 ProblemDetail 스타일 오류 응답을 파싱
- 개발 서버는 `/api/*`를 `localhost:8080`(Pinpoint 백엔드)으로 프록시
- 라우트 경로는 `packages/ui/src/constants/path.ts`의 `APP_PATH`에 정의

### UI 컴포넌트

- `packages/ui/src/components/ui/` — Tailwind로 스타일링된 Radix UI 프리미티브 (shadcn/ui 패턴: accordion, dialog, dropdown, tooltip 등)
- `packages/ui/src/components/` — 도메인 컴포넌트 (ServerMap, Chart, Transaction, Inspector 등)
- 스타일링: `class-variance-authority` + `clsx` + `tailwind-merge`를 사용하는 Tailwind CSS

### 시각화 라이브러리

- **scatter-chart**: 독립형 캔버스 기반 스캐터 플롯, 코어에 React 없음
- **server-map**: Cytoscape + dagre 레이아웃으로 애플리케이션 의존성 토폴로지 표시
- 두 라이브러리 모두 `packages/ui/src/components/`의 래퍼 컴포넌트에서 사용됨

## 코드 스타일

- Prettier: 100자 너비, 작은따옴표, 후행 쉼표, 세미콜론
- ESLint: flat config (`eslint.config.js`), TypeScript-ESLint + Prettier 통합
- TypeScript strict 모드, ESNext 타겟, `react-jsx` 변환
- `@pinpoint-fe/ui`의 임포트는 딥 패스 사용: `@pinpoint-fe/ui/src/constants`, `@pinpoint-fe/ui/src/hooks` 등
- ServerMap(기본 라우트)을 제외한 모든 페이지는 `React.lazy()`로 지연 로딩
- i18n: `packages/ui/src/constants/locales/`에 영어와 한국어 로케일을 포함하는 `i18next`

## `.claude/` 리소스 탐색 (중요)

`.claude/`의 설정 파일(스킬, 규칙, 명령어, 설정 등)은 이 모노레포의 여러 디렉토리에 분산되어 있습니다. **`.claude/` 리소스를 찾을 때**, 현재 작업 디렉토리에서 찾지 못하면 상위 디렉토리로 올라가며 탐색합니다:

1. 먼저 `<cwd>/.claude/` 확인
2. 없으면 상위 디렉토리 탐색: 루트 `.claude/`
3. 알려진 위치:
   - `.claude/skills/` — add-translation, create-api-hook, create-component, create-page, review-code, qa-pr, write-test, mvn-web
   - `.claude/agents/` — code-reviewer, explorer, debugger, qa-engineer
   - `.claude/rules/` — code-style, monorepo, git-workflow, tool-usage, code-review-policy

이 규칙은 **모든** `.claude/` 리소스(스킬, 규칙, 명령어, 기타 설정)에 적용됩니다.

## 빌드 통합

웹 앱 출력은 Java/Maven 백엔드 내에 정적 자산으로 배포됩니다. `yarn move:dist`는 `dist/`를 `target/classes/static/`으로 복사합니다. `BASE_PATH` 환경 변수로 서빙 서브 경로를 제어합니다.
