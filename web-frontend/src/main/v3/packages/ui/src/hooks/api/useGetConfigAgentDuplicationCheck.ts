import useSWR from 'swr';
import { ConfigAgentDuplicationCheck, END_POINTS } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';

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

  return useSWR<ConfigAgentDuplicationCheck.Response>(
    queryString ? `${END_POINTS.CONFIG_AGENT_DUPLICATION_CHECK}${queryString}` : null,
    {
      ...swrConfigs,
      suspense: false,
    },
  );
};
