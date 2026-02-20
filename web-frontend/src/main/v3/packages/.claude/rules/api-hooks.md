---
paths:
  - "packages/ui/src/hooks/api/**/*.ts"
  - "packages/ui/src/constants/EndPoints.ts"
  - "packages/ui/src/constants/types/**/*.ts"
---

# API Hook Rules

## Creating a New API Hook
1. Define endpoint in `packages/ui/src/constants/EndPoints.ts`
2. Define response type in `packages/ui/src/constants/types/` using namespace pattern
3. Create hook in `packages/ui/src/hooks/api/` following this pattern:

```typescript
import { useQuery } from '@tanstack/react-query';
import { END_POINTS, GetSomething } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

export const useGetSomething = (params: GetSomething.Parameters) => {
  const queryString = convertParamsToQueryString(params);

  return useQuery<GetSomething.Response>({
    queryKey: [END_POINTS.SOMETHING, queryString],
    queryFn: queryFn(`${END_POINTS.SOMETHING}?${queryString}`),
    enabled: !!params.applicationName,
    gcTime: 30000,
  });
};
```

### Caller Example
```typescript
import { useSearchParameters } from '@pinpoint-fe/ui/src/hooks/searchParameters';
import { useGetSomething } from '@pinpoint-fe/ui/src/hooks/api/useGetSomething';

const MyComponent = () => {
  const { application, dateRange } = useSearchParameters();

  const { data } = useGetSomething({
    applicationName: application?.applicationName || '',
    from: dateRange.from.getTime(),
    to: dateRange.to.getTime(),
  });
};
```

### Important
- **DO NOT** call `useSearchParameters()` or `useServerMapSearchParameters()` inside API hooks
- API hooks receive all needed parameters explicitly from the caller
- This keeps hooks decoupled from URL/router state and easier to test

## Error Handling
- `queryFn` from `reactQueryHelper.ts` automatically parses RFC 7807 ProblemDetail errors
- Error responses include: `type`, `title`, `status`, `detail`, `instance`, `trace`
- Do NOT wrap queryFn in try/catch — let React Query handle error states

## Endpoint Naming
- All endpoints prefixed with `/api`
- Constant names use SCREAMING_SNAKE_CASE matching the path
- Export from `END_POINTS` object in `EndPoints.ts`

## Type Definitions
- Use namespace pattern for API types: `namespace GetSomething { interface Parameters {...}; interface Response {...} }`
- Place in `packages/ui/src/constants/types/`
- Export from `packages/ui/src/constants/types/index.ts`
