import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, AgentManagementList } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: AgentManagementList.Parameters) => {
  if (queryParams.applicationName && (queryParams.serviceTypeName || queryParams.serviceTypeCode)) {
    // from/to 없이 호출하면 백엔드가 전체 agent 목록(현시점 상태)을 반환한다.
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export const useGetAgentManagementList = ({
  applicationName,
  serviceTypeName,
  serviceTypeCode,
}: AgentManagementList.Parameters) => {
  const queryString = getQueryString({ applicationName, serviceTypeName, serviceTypeCode });

  const { data, isLoading, refetch } = useSuspenseQuery<AgentManagementList.Response | null>({
    queryKey: [END_POINTS.AGENT_LIST, queryString],
    queryFn: queryString ? queryFn(`${END_POINTS.AGENT_LIST}${queryString}`) : () => null,
  });

  return {
    data: [...(data || [])].sort((a, b) => a.agentId.localeCompare(b.agentId)),
    isLoading,
    refetch,
  };
};
