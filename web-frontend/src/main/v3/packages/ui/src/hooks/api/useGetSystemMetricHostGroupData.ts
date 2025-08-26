import { END_POINTS, SystemMetricHostGroup } from '@pinpoint-fe/ui/src/constants';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

export const useGetSystemMetricHostGroupData = () => {
  const { data, isLoading, isFetching, refetch } = useSuspenseQuery<SystemMetricHostGroup.Response>(
    {
      queryKey: [END_POINTS.SYSTEM_METRIC_HOST_GROUP],
      queryFn: queryFn(`${END_POINTS.SYSTEM_METRIC_HOST_GROUP}`),
    },
  );

  return { data, isLoading, refetch, isValidating: isFetching };
};
