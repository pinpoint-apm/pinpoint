import { keepPreviousData, useQuery } from '@tanstack/react-query';
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
  /**
   * 실시간 보기 여부(기본값 false).
   *
   * 실시간에서는 화면이 2초마다 from/to를 밀어 새 쿼리 키로 조회한다. 그래서
   * (1) 다시 볼 일 없는 이전 구간의 응답이 캐시에 쌓이지 않도록 gcTime을 0으로 두고,
   * (2) 새 응답이 오기 전까지 이전 map을 그대로 보여줘 화면이 깜빡이지 않게 한다.
   * `useGetServerMapDataV2`의 shouldPoll과 같은 처리다.
   */
  shouldPoll?: boolean;
}

export const useGetServiceMap = (
  params: GetServiceMap.Parameters,
  { requiresApplication = true, shouldPoll = false }: UseGetServiceMapOptions = {},
) => {
  const queryString = convertParamsToQueryString(params);

  return useQuery<GetServiceMap.Response>({
    queryKey: [END_POINTS.SERVICE_MAP_DATA, queryString],
    queryFn: queryFn(`${END_POINTS.SERVICE_MAP_DATA}?${queryString}`),
    enabled: (!requiresApplication || !!params.applicationName) && !!params.from && !!params.to,
    gcTime: shouldPoll ? 0 : 30000,
    placeholderData: shouldPoll ? keepPreviousData : undefined,
  });
};
