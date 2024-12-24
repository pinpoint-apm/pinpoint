import { useQuery } from '@tanstack/react-query';
import { END_POINTS, SearchApplication } from '@pinpoint-fe/ui/constants';
import { queryFn } from './reactQueryHelper';

export const useGetAgentsStatistics = (load: boolean) => {
  const { data, isLoading, refetch } = useQuery<SearchApplication.Response>({
    queryKey: [END_POINTS.AGENT_STATISTICS],
    queryFn: queryFn(`${END_POINTS.AGENT_STATISTICS}`),
    enabled: !!load,
  });

  return { data, isLoading, refetch };
};
