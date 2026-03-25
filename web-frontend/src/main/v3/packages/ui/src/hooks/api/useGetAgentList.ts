import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { AgentList } from '@pinpoint-fe/ui/src/constants/types/AgentList';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<AgentList.Parameters>) => {
  const hasServiceType = queryParams.serviceTypeName || queryParams.serviceTypeCode !== undefined;
  if (queryParams.applicationName && queryParams.from && queryParams.to && hasServiceType) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export const useGetAgentList = ({
  applicationName,
  serviceTypeName,
  serviceTypeCode,
  from,
  to,
}: AgentList.Parameters) => {
  const queryString = getQueryString({
    applicationName,
    serviceTypeName,
    serviceTypeCode,
    from,
    to,
  });

  const { data, isLoading, refetch } = useSuspenseQuery<AgentList.Response | null>({
    queryKey: [END_POINTS.AGENT_LIST, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.AGENT_LIST}${queryString}`) : () => null,
  });

  return {
    data: [...(data || [])].sort((a, b) => a.agentId.localeCompare(b.agentId)),
    isLoading,
    refetch,
  };
};
