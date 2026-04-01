---
name: create-api-hook
description: useGetXxx 패턴에 따라 새 React Query API 훅을 생성합니다. 새 백엔드 API를 연동하거나, 엔드포인트에 대한 훅이 필요하거나, 데이터 페칭 로직을 추가할 때 반드시 이 스킬을 사용하세요.
---

# 새 API 훅 생성

`$ARGUMENTS` 설명을 가진 `$0` 엔드포인트에 대한 새 API 훅을 생성합니다.

다음 단계를 따르세요:

## 1. 기존 패턴 분석
- `packages/ui/src/hooks/api/reactQueryHelper.ts`를 읽어 queryFn과 queryClient 이해
- `packages/ui/src/constants/EndPoints.ts`에서 엔드포인트 네이밍 확인
- `packages/ui/src/hooks/api/`의 유사한 기존 훅에서 패턴 파악

## 2. 엔드포인트 정의
`packages/ui/src/constants/EndPoints.ts`에 엔드포인트 상수 추가:
```typescript
export const NEW_ENDPOINT = `/api/path/to/endpoint`;
```

## 3. 타입 정의
`packages/ui/src/constants/types/`에 타입 생성 또는 확장:
```typescript
export namespace GetNewData {
  export interface Parameters {
    applicationName: string;
    from: number;
    to: number;
    // ... 기타 파라미터
  }
  export interface Response {
    // ... 응답 형태
  }
}
```
`packages/ui/src/constants/types/index.ts`에서 익스포트.

## 4. 훅 생성
`packages/ui/src/hooks/api/useGetNewData.ts` 생성:
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

## 5. 훅 익스포트
`packages/ui/src/hooks/api/index.ts` (또는 해당하는 배럴 파일)에서 익스포트.

## 6. 핵심 가이드라인
- 필수 파라미터 없이 쿼리가 실행되지 않도록 `enabled` 사용
- `reactQueryHelper.ts`의 `queryFn` 사용 — 직접 fetch를 구현하지 마세요
- 쿼리 키에는 응답에 영향을 미치는 엔드포인트와 모든 파라미터 포함
- POST 뮤테이션은 `useQuery` 대신 `useMutation` 사용
- 폴링의 경우 `gcTime: 0` 설정, 부드러운 전환을 위해 `placeholderData: (prev) => prev` 사용
