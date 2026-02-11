import { useQuery } from '@tanstack/react-query';
import { END_POINTS, AgentOverview } from '@pinpoint-fe/ui/src/constants';
import { queryFn } from './reactQueryHelper';

export const useGetAgentsStatistics = (load: boolean) => {
  const { data, isLoading, refetch } = useQuery<AgentOverview.Response>({
    queryKey: [END_POINTS.AGENT_STATISTICS],
    queryFn: queryFn(`${END_POINTS.AGENT_STATISTICS}`),
    enabled: !!load,
  });

  return { data, isLoading, refetch };
};
