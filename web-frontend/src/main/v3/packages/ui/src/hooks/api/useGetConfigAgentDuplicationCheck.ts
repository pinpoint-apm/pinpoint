import { useQuery } from '@tanstack/react-query';
import { ConfigAgentDuplicationCheck, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<ConfigAgentDuplicationCheck.Parameters>) => {
  if (queryParams.agentId) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

export const useGetConfigAgentDuplicationCheck = ({ agentId }: { agentId: string }) => {
  const queryParams = {
    agentId,
  };
  const queryString = getQueryString(queryParams);

  return useQuery<ConfigAgentDuplicationCheck.Response>({
    queryKey: [END_POINTS.CONFIG_AGENT_DUPLICATION_CHECK, queryString],
    queryFn: queryFn(`${END_POINTS.CONFIG_AGENT_DUPLICATION_CHECK}${queryString}`),
    enabled: !!queryString,
    retry: false,
  });
};
