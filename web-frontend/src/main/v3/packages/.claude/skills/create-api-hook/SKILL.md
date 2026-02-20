---
name: create-api-hook
description: Create a new React Query API hook following the useGetXxx pattern. Use when adding a new backend API integration.
argument-hint: "[endpoint-path] [description]"
allowed-tools: Read, Glob, Grep, Edit, Write
---

# Create a New API Hook

Create a new API hook for endpoint `$0` with description: `$ARGUMENTS`.

Follow these steps:

## 1. Analyze Existing Patterns
- Read `packages/ui/src/hooks/api/reactQueryHelper.ts` to understand queryFn and queryClient
- Read `packages/ui/src/constants/EndPoints.ts` for endpoint naming
- Read a similar existing hook in `packages/ui/src/hooks/api/` for the pattern

## 2. Define Endpoint
Add the endpoint constant to `packages/ui/src/constants/EndPoints.ts`:
```typescript
export const NEW_ENDPOINT = `/api/path/to/endpoint`;
```

## 3. Define Types
Create or extend types in `packages/ui/src/constants/types/`:
```typescript
export namespace GetNewData {
  export interface Parameters {
    applicationName: string;
    from: number;
    to: number;
    // ... other params
  }
  export interface Response {
    // ... response shape
  }
}
```
Export from `packages/ui/src/constants/types/index.ts`.

## 4. Create Hook
Create `packages/ui/src/hooks/api/useGetNewData.ts`:
```typescript
import { useQuery } from '@tanstack/react-query';
import { END_POINTS, GetNewData } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

export const useGetNewData = (params: GetNewData.Parameters) => {
  const queryString = convertParamsToQueryString(params);

  return useQuery<GetNewData.Response>({
    queryKey: [END_POINTS.NEW_ENDPOINT, queryString],
    queryFn: queryFn(`${END_POINTS.NEW_ENDPOINT}?${queryString}`),
    enabled: !!params.applicationName,
    gcTime: 30000,
  });
};
```

## 5. Export Hook
Export from `packages/ui/src/hooks/api/index.ts` (or equivalent barrel file).

## 6. Key Guidelines
- Use `enabled` to prevent queries without required params
- Use `queryFn` from `reactQueryHelper.ts` — never roll your own fetch
- Query key must include the endpoint and all parameters that affect the response
- For POST mutations, use `useMutation` instead of `useQuery`
- For polling, set `gcTime: 0` and use `placeholderData: (prev) => prev` for smooth transitions
