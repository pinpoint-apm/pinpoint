# Code Reviewer Memory

## Key Architectural Patterns (Confirmed)

### withInitialFetch HOC
- `packages/ui/src/components/HOC/withInitialFetch.tsx`
- 역할: configuration 조회, searchParameters 동기화, error 시 /apiCheck 리다이렉트
- 현재 커밋(4e4bbdbf55)에서 `InitialFetchOutlet`으로 대체됨
- HOC 자체는 아직 삭제되지 않음 (dead code)

### 새로운 레이아웃 라우트 패턴 (4e4bbdbf55 이후)
- SideNavigationOutlet > InitialFetchOutlet > (errorElement wrapper) > 개별 페이지
- Configuration 필요 페이지는 추가로 ConfigurationOutlet 중첩
- 각 페이지 컴포넌트는 useAtomValue(configurationAtom)으로 직접 atom 읽기

### 타입 캐스팅 불일치 패턴 (주의)
- `FilteredMapPageProps.configuration`: `Configuration & Record<string, string>`
- `apps/web/pages/FilteredMap.tsx`에서 `Record<string, string>`으로 올바르게 캐스팅
- `RealtimePage`도 동일하게 `Record<string, string>` 필요
- 일부 페이지는 `Record<string, unknown>` 사용 (느슨한 캐스팅)

### navigate()를 render 중 호출하는 패턴
- React Router v6에서 render 단계에서 navigate()를 직접 호출하면 경고 발생
- `useEffect` 안에서 navigate를 호출해야 안전
- InitialFetchOutlet이 이 문제를 가지고 있음

## 주요 파일 경로
- `apps/web/src/routes/index.tsx` - 라우팅 설정
- `apps/web/src/components/Layout/InitialFetchOutlet.tsx` - 핵심 초기화 로직
- `packages/ui/src/components/HOC/withInitialFetch.tsx` - 구 HOC (현재 dead code)
- `packages/ui/src/atoms/searchParameters.ts` - 검색 파라미터 atom
- `apps/web/src/hooks/useMenuItems.tsx` - 사이드 네비게이션 메뉴
