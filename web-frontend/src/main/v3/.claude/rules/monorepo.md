# 모노레포 규칙

## 패키지 구조
- `apps/web/` (`@pinpoint-fe/web`): 메인 앱 — 얇은 페이지 래퍼 + 라우팅만 담당
- `packages/ui/` (`@pinpoint-fe/ui`): 핵심 라이브러리 — 모든 컴포넌트, 훅, 아톰, 상수
- `packages/scatter-chart/` (`@pinpoint-fe/scatter-chart`): 캔버스 기반 스캐터 차트
- `packages/server-map/` (`@pinpoint-fe/server-map`): Cytoscape 토폴로지 그래프
- `packages/datetime-picker/` (`@pinpoint-fe/datetime-picker`): 날짜/시간 선택기

## 의존성 규칙
- `apps/web`은 `@pinpoint-fe/ui`에 의존 — 딥 패스로 임포트
- `@pinpoint-fe/ui`는 scatter-chart, server-map, datetime-picker에 의존
- 내부 의존성은 package.json에서 `"*"` 버전 사용
- Yarn 워크스페이스가 로컬 개발을 위해 패키지를 연결

## 코드 위치 가이드
- **새 컴포넌트**: `packages/ui/src/components/`
- **새 훅**: `packages/ui/src/hooks/`
- **새 상수/타입**: `packages/ui/src/constants/`
- **새 아톰**: `packages/ui/src/atoms/`
- **새 유틸리티**: `packages/ui/src/utils/`
- **새 페이지**: 페이지 컴포넌트는 `packages/ui/src/pages/`, 래퍼는 `apps/web/src/pages/`
- **새 라우트**: `apps/web/src/routes/index.tsx`

## 명령어
- 패키지별 명령어는 항상 `yarn workspace <package-name>` 사용
- 루트의 `yarn dev`, `yarn build`, `yarn lint`, `yarn test`는 적절한 워크스페이스에 위임
