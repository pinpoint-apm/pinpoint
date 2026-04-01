---
paths:
  - "packages/ui/src/atoms/**/*.ts"
  - "packages/ui/src/hooks/**/*.ts"
  - "packages/ui/src/hooks/**/*.tsx"
---

# 상태 관리 규칙

## 아키텍처 (3계층)
1. **URL 상태 (진실의 원천)**: 애플리케이션 이름, 날짜 범위(from/to), 쿼리 필터
2. **Jotai 아톰 (전역 상태)**: `InitialFetchOutlet`을 통해 URL에서 동기화, UI 상태 포함
3. **React Query (서버 상태)**: 캐싱을 포함한 모든 API 데이터 페칭

## Jotai 아톰 (`packages/ui/src/atoms/`)
- 아톰 네이밍: `Atom` 접미사의 camelCase (예: `serverMapDataAtom`)
- 파생 아톰은 `atom((get) => ...)` 패턴 사용
- 아톰은 최소화 — 여러 컴포넌트에서 필요한 것만 저장
- 아톰은 `InitialFetchOutlet` 레이아웃 라우트를 통해 URL 검색 파라미터에서 동기화
- 핵심 아톰: `searchParametersAtom`, `configurationAtom`, `serverMapDataAtom`, `serverMapCurrentTargetAtom`

## React Query 훅 (`packages/ui/src/hooks/api/`)
- 훅 네이밍: GET은 `useGetXxx`, POST는 `usePostXxx`, PATCH는 `usePatchXxx`
- 쿼리 키 패턴: `[END_POINTS.ENDPOINT_NAME, queryString]`
- `reactQueryHelper.ts`의 공유 `queryFn` 사용 — ProblemDetail 오류 파싱을 처리함
- 필수 파라미터 없이 쿼리가 실행되지 않도록 항상 `enabled` 옵션 설정
- 기본 캐시 설정: `gcTime: 30000` (30초), `staleTime: 3000` (3초)
- 폴링/실시간: `gcTime: 0`, 부드러운 전환을 위해 `placeholderData: (prev) => prev`
- 엔드포인트는 `packages/ui/src/constants/EndPoints.ts`에 정의

## URL 검색 파라미터
- 페이지별 전용 훅: `useServerMapSearchParameters`, `useErrorAnalysisSearchParameters` 등
- `packages/ui/src/hooks/searchParameters/`에 위치
- 파싱: pathname에서 애플리케이션 이름, query string에서 날짜 범위 + 필터
- React Router 로더 (`packages/ui/src/loader/`)가 유효하지 않은 날짜를 검증하고 리다이렉트

## 금지사항
- 아톰에 파생 상태 저장 금지 — 컴포넌트 또는 파생 아톰에서 계산
- 아톰에 서버 데이터 중복 저장 금지 — React Query가 관리하도록 위임
- URL 파라미터 수동 동기화 금지 — `InitialFetchOutlet`과 검색 파라미터 훅 사용
