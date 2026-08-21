import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, AgentOverview } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<AgentOverview.Parameters>) => {
  if (queryParams.application && (queryParams.serviceTypeName || queryParams.serviceTypeCode)) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

/**
 * @param serviceName 이 조회가 나갈 service. 지정하면 `pServiceName` 헤더를 직접 싣고 캐시도 그
 * 단위로 갈린다. 화면의 service와 조회 대상의 service가 다를 때 넘긴다 — servicemap은 다른
 * service의 application도 함께 그리기 때문이다(`useServerMapTargetServiceName`). 넘기지 않으면
 * 기존대로 fetch 인터셉터가 경로/전역 선택값으로 결정한다.
 */
export const useGetAgentOverview = (
  {
    application,
    serviceTypeName,
    serviceTypeCode,
    from,
    to,
    applicationPairs,
  }: AgentOverview.Parameters,
  serviceName?: string,
) => {
  const queryString = getQueryString({
    from,
    to,
    application,
    serviceTypeName,
    serviceTypeCode,
    applicationPairs,
  });

  const { data, isLoading, refetch } = useSuspenseQuery<AgentOverview.Response | null>({
    queryKey: [END_POINTS.AGENT_OVERVIEW, queryString, serviceName],
    queryFn: !!queryString
      ? queryFn(`${END_POINTS.AGENT_OVERVIEW}${queryString}`, { serviceName })
      : () => null,
  });

  return {
    data: [...(data || [])]?.sort((a, b) => {
      return a.agentId.localeCompare(b.agentId);
    }),
    isLoading,
    refetch,
  };
};
