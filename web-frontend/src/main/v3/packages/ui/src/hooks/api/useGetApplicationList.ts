import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import React from 'react';
import { queryClient } from './reactQueryHelper';

let cachedETag: string | null = null;

const applicationListQueryFn = async (clearCache?: boolean) => {
  const url = clearCache
    ? `${END_POINTS.APPLICATION_LIST}?clearCache=true`
    : END_POINTS.APPLICATION_LIST;

  const headers: HeadersInit = {};
  if (cachedETag) {
    headers['If-None-Match'] = cachedETag;
  }

  const response = await fetch(url, {
    cache: 'no-store',
    headers,
  });

  if (response.status === 304) {
    return queryClient.getQueryData([END_POINTS.APPLICATION_LIST]);
  }

  if (!response.ok) {
    throw new Error(
      `Request failed with status ${response.status}. An error occurred while fetching the data.`,
    );
  }

  const etag = response.headers.get('ETag');
  if (etag) {
    cachedETag = etag;
  }

  return response.json();
};

export const useGetApplicationList = (shouldFetch = true) => {
  const clearCacheRef = React.useRef(false);

  const query = useQuery({
    queryKey: [END_POINTS.APPLICATION_LIST],
    queryFn: () => {
      const shouldClearCache = clearCacheRef.current;
      clearCacheRef.current = false;
      return applicationListQueryFn(shouldClearCache);
    },
    enabled: shouldFetch,
  });

  const refetchWithClearCache = React.useCallback(() => {
    clearCacheRef.current = true;
    query.refetch();
  }, [query]);

  return { ...query, refetchWithClearCache };
};
