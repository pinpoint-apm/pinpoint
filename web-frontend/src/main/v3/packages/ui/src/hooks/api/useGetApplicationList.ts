import { useQuery } from '@tanstack/react-query';
import { useAtomValue } from 'jotai';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import React from 'react';
import { queryFn } from './reactQueryHelper';
import { resolveRequestService } from './serviceNameFetchInterceptor';

/**
 * ETag 재검증은 브라우저 HTTP 캐시에 맡긴다. 백엔드(`MainController#getApplicationGroup`)가
 * `Vary: pServiceName`과 `Cache-Control: no-cache`를 보내므로 브라우저가
 * (1) service별로 응답을 따로 보관하고 (2) 매 요청에 `If-None-Match`를 자동으로 실으며
 * (3) 304를 받으면 보관해 둔 본문을 200처럼 돌려준다. 그래서 이 훅은 304를 볼 일이 없다.
 *
 * 예전에는 `cache: 'no-store'`로 브라우저 캐시를 끄고 ETag를 service별 Map에 직접 들고 있었다.
 * 응답이 URL이 아니라 pServiceName 헤더에 따라 달라지는데 백엔드가 `Vary`를 보내지 않아,
 * URL만 보는 브라우저 캐시가 다른 service의 목록을 재사용했기 때문이다. 이제 백엔드가 `Vary`를
 * 보내므로 직접 관리할 이유가 없다.
 */
export const useGetApplicationList = (shouldFetch = true) => {
  // selectedService가 바뀌면 queryKey가 달라져 새 service의 목록을 다시 불러온다.
  // service마다 application 목록이 다르므로 캐시를 service별로 분리한다.
  const selectedService = useAtomValue(selectedServiceAtom);
  // 이 쿼리는 사이드 네비게이션(remount 대상 밖)에서도 쓰이므로, queryKeyHashFn에만 의존하면
  // service가 바뀌어도 리렌더가 없어 재조회가 일어나지 않는다. 요청에 쓰이는 service를
  // queryKey에 명시해 직접 다룬다.
  const service = resolveRequestService(selectedService);
  const clearCacheRef = React.useRef(false);

  const query = useQuery({
    queryKey: [END_POINTS.APPLICATION_LIST, service],
    queryFn: () => {
      const shouldClearCache = clearCacheRef.current;
      clearCacheRef.current = false;
      // clearCache 요청은 URL이 달라 브라우저 캐시 엔트리도 따로 잡히고, 백엔드가 이 파라미터를
      // 보면 ETag와 무관하게 서버 캐시를 비우고 목록을 다시 만들어 항상 200으로 돌려준다.
      return queryFn(
        shouldClearCache
          ? `${END_POINTS.APPLICATION_LIST}?clearCache=true`
          : END_POINTS.APPLICATION_LIST,
      )();
    },
    enabled: shouldFetch,
    refetchOnMount: false,
  });

  const { refetch } = query;
  const refetchWithClearCache = React.useCallback(() => {
    clearCacheRef.current = true;
    refetch();
  }, [refetch]);

  return { ...query, refetchWithClearCache };
};
