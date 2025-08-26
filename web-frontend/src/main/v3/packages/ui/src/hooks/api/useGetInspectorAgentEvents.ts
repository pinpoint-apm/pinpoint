import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, InspectorAgentEvents } from '@pinpoint-fe/ui/src/constants';
import { queryFn } from './reactQueryHelper';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
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

  return useSuspenseQuery<InspectorAgentEvents.Response | null>({
    queryKey: [END_POINTS.INSPECTOR_AGENT_EVENTS, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.INSPECTOR_AGENT_EVENTS}${queryString}`)
      : () => null,
  });
};
