# Code Reviewer Memory

## Key Architectural Patterns (Confirmed)

### Nested Layout Route Pattern
- `apps/web/src/components/Layout/InitialFetchOutlet.tsx` — configuration 조회, searchParameters 동기화, error 시 /apiCheck 리다이렉트
- `apps/web/src/components/Layout/SideNavigationOutlet.tsx` — 사이드 네비게이션 레이아웃
- `apps/web/src/components/Layout/ConfigurationOutlet.tsx` — 설정 페이지 레이아웃
- 라우트 구조: SideNavigationOutlet > InitialFetchOutlet > (errorElement wrapper) > 개별 페이지
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
- InitialFetchOutlet에서는 useEffect로 처리됨

## 주요 파일 경로
- `apps/web/src/routes/index.tsx` - 라우팅 설정
- `apps/web/src/components/Layout/InitialFetchOutlet.tsx` - 핵심 초기화 로직
- `packages/ui/src/atoms/searchParameters.ts` - 검색 파라미터 atom
- `apps/web/src/hooks/useMenuItems.tsx` - 사이드 네비게이션 메뉴
