---
paths:
  - "packages/ui/src/hooks/api/**/*.ts"
  - "packages/ui/src/constants/EndPoints.ts"
  - "packages/ui/src/constants/types/**/*.ts"
---

# API 훅 규칙

## 새 API 훅 생성 방법
1. `packages/ui/src/constants/EndPoints.ts`에 엔드포인트 정의
2. `packages/ui/src/constants/types/`에 네임스페이스 패턴으로 응답 타입 정의
3. `packages/ui/src/hooks/api/`에 다음 패턴으로 훅 생성:

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

### 호출자 예시
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

### 중요사항
- API 훅 내부에서 `useSearchParameters()` 또는 `useServerMapSearchParameters()`를 **호출하지 마세요**
- API 훅은 필요한 모든 파라미터를 호출자로부터 명시적으로 받습니다
- 이를 통해 훅이 URL/라우터 상태와 분리되어 테스트하기 쉬워집니다

## 오류 처리
- `reactQueryHelper.ts`의 `queryFn`은 RFC 7807 ProblemDetail 오류를 자동으로 파싱
- 오류 응답 포함: `type`, `title`, `status`, `detail`, `instance`, `trace`
- queryFn을 try/catch로 감싸지 마세요 — React Query가 오류 상태를 처리합니다

## 엔드포인트 네이밍
- 모든 엔드포인트는 `/api` 접두사
- 상수 이름은 경로에 맞는 SCREAMING_SNAKE_CASE 사용
- `EndPoints.ts`의 `END_POINTS` 객체에서 익스포트

## 타입 정의
- API 타입에 네임스페이스 패턴 사용: `namespace GetSomething { interface Parameters {...}; interface Response {...} }`
- `packages/ui/src/constants/types/`에 위치
- `packages/ui/src/constants/types/index.ts`에서 익스포트
