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

export const useGetAgentOverview = ({
  application,
  serviceTypeName,
  serviceTypeCode,
  from,
  to,
  applicationPairs,
}: AgentOverview.Parameters) => {
  const queryString = getQueryString({
    from,
    to,
    application,
    serviceTypeName,
    serviceTypeCode,
    applicationPairs,
  });

  const { data, isLoading, refetch } = useSuspenseQuery<AgentOverview.Response | null>({
    queryKey: [END_POINTS.AGENT_OVERVIEW, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.AGENT_OVERVIEW}${queryString}`) : () => null,
  });

  return {
    data: [...(data || [])]?.sort((a, b) => {
      return a.agentId.localeCompare(b.agentId);
    }),
    isLoading,
    refetch,
  };
};
