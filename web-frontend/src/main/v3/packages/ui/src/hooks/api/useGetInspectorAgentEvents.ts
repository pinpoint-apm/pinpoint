import useSWR from 'swr';
import { END_POINTS, InspectorAgentEvents } from '@pinpoint-fe/ui/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';
import { useInspectorSearchParameters } from '../searchParameters';

const getQueryString = (queryParams: Partial<InspectorAgentEvents.Parameters>) => {
  if (queryParams.agentId && queryParams.from && queryParams.to) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

export const useGetInspectorAgentEvents = ({ range: [from, to] }: { range: number[] }) => {
  const { agentId } = useInspectorSearchParameters();
  const queryParams = {
    agentId,
    from,
    to,
    exclude: '10199', // it's derived from the legacy
  };
  const queryString = getQueryString(queryParams);

  return useSWR<InspectorAgentEvents.Response>(
    queryString ? `${END_POINTS.INSPECTOR_AGENT_EVENTS}${queryString}` : null,
    swrConfigs,
  );
};
