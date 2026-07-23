import { useQuery } from '@tanstack/react-query';
import { useAtomValue } from 'jotai';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import React from 'react';
import { queryClient } from './reactQueryHelper';

// ETag는 service별로 응답이 다를 수 있으므로 service마다 따로 보관한다.
// 하나의 전역 값으로 두면 service 전환 시 이전 service의 ETag를 보내
// 백엔드가 304를 돌려주며 옛 목록을 그대로 재사용하는 문제가 생긴다.
const cachedETagByService = new Map<string, string>();

const applicationListQueryFn = async (service: string, clearCache?: boolean) => {
  const url = clearCache
    ? `${END_POINTS.APPLICATION_LIST}?clearCache=true`
    : END_POINTS.APPLICATION_LIST;

  const headers: HeadersInit = {};
  if (clearCache) {
    cachedETagByService.delete(service);
  }
  const cachedETag = cachedETagByService.get(service);
  if (cachedETag) {
    headers['If-None-Match'] = cachedETag;
  }

  const response = await fetch(url, {
    cache: 'no-store',
    headers,
  });

  if (response.status === 304) {
    const cached = queryClient.getQueryData([END_POINTS.APPLICATION_LIST, service]);
    if (cached !== undefined) return cached;
    cachedETagByService.delete(service);
    return applicationListQueryFn(service, clearCache);
  }

  if (!response.ok) {
    throw new Error(
      `Request failed with status ${response.status}. An error occurred while fetching the data.`,
    );
  }

  const etag = response.headers.get('ETag');
  if (etag) {
    cachedETagByService.set(service, etag);
  }

  return response.json();
};

export const useGetApplicationList = (shouldFetch = true) => {
  // selectedService가 바뀌면 queryKey가 달라져 새 service의 목록을 다시 불러온다.
  // service마다 application 목록이 다르므로 캐시를 service별로 분리한다.
  const selectedService = useAtomValue(selectedServiceAtom);
  const clearCacheRef = React.useRef(false);

  const query = useQuery({
    queryKey: [END_POINTS.APPLICATION_LIST, selectedService],
    queryFn: () => {
      const shouldClearCache = clearCacheRef.current;
      clearCacheRef.current = false;
      return applicationListQueryFn(selectedService, shouldClearCache);
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
