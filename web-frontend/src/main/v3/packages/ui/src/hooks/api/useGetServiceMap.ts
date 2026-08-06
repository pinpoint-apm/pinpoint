import { useQuery } from '@tanstack/react-query';
import { END_POINTS, GetServiceMap } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

export interface UseGetServiceMapOptions {
  /**
   * application을 골라야만 조회할지 여부(기본값 true).
   * DEFAULT가 아닌 service는 백엔드가 service에 소속된 모든 application을 모아 map을 그리므로,
   * application을 고르지 않아도 조회할 수 있도록 false를 넘긴다.
   */
  requiresApplication?: boolean;
}

export const useGetServiceMap = (
  params: GetServiceMap.Parameters,
  { requiresApplication = true }: UseGetServiceMapOptions = {},
) => {
  const queryString = convertParamsToQueryString(params);

  return useQuery<GetServiceMap.Response>({
    queryKey: [END_POINTS.SERVICE_MAP_DATA, queryString],
    queryFn: queryFn(`${END_POINTS.SERVICE_MAP_DATA}?${queryString}`),
    enabled: (!requiresApplication || !!params.applicationName) && !!params.from && !!params.to,
    gcTime: 30000,
  });
};
