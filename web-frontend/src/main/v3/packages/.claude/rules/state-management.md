---
paths:
  - "packages/ui/src/atoms/**/*.ts"
  - "packages/ui/src/hooks/**/*.ts"
  - "packages/ui/src/hooks/**/*.tsx"
---

# State Management Rules

## Architecture (Three Layers)
1. **URL State (Source of Truth)**: Application name, date range (from/to), query filters
2. **Jotai Atoms (Global State)**: Synced from URL via `withInitialFetch`, plus UI state
3. **React Query (Server State)**: All API data fetching with caching

## Jotai Atoms (`packages/ui/src/atoms/`)
- Atom naming: camelCase with `Atom` suffix (e.g., `serverMapDataAtom`)
- Derived atoms use `atom((get) => ...)` pattern
- Keep atoms minimal — store only what multiple components need
- Atoms are synced from URL search params via `withInitialFetch` HOC
- Key atoms: `searchParametersAtom`, `configurationAtom`, `serverMapDataAtom`, `serverMapCurrentTargetAtom`

## React Query Hooks (`packages/ui/src/hooks/api/`)
- Hook naming: `useGetXxx` for GET, `usePostXxx` for POST, `usePatchXxx` for PATCH
- Query key pattern: `[END_POINTS.ENDPOINT_NAME, queryString]`
- Use shared `queryFn` from `reactQueryHelper.ts` — it handles ProblemDetail error parsing
- Always set `enabled` option to prevent queries from firing without required params
- Default cache settings: `gcTime: 30000` (30s), `staleTime: 3000` (3s)
- For polling/realtime: `gcTime: 0`, `placeholderData: (prev) => prev` for smooth transitions
- Endpoints defined in `packages/ui/src/constants/EndPoints.ts`

## URL Search Parameters
- Dedicated hooks per page: `useServerMapSearchParameters`, `useErrorAnalysisSearchParameters`, etc.
- Located in `packages/ui/src/hooks/searchParameters/`
- Parse: application name from pathname, date range + filters from query string
- React Router loaders (`packages/ui/src/loader/`) validate and redirect invalid dates

## Do NOT
- Store derived state in atoms — compute it in components or derived atoms
- Duplicate server data in atoms — let React Query manage it
- Manually sync URL params — use `withInitialFetch` and search parameter hooks
