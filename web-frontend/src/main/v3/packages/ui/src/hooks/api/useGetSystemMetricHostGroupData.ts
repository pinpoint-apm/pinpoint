import useSWR from 'swr';
import { END_POINTS, SystemMetricHostGroup } from '@pinpoint-fe/ui/src/constants';
import { swrConfigs } from './swrConfigs';

export const useGetSystemMetricHostGroupData = () => {
  const { data, isLoading, mutate, isValidating } = useSWR<SystemMetricHostGroup.Response>(
    `${END_POINTS.SYSTEM_METRIC_HOST_GROUP}`,
    swrConfigs,
  );

  return { data, isLoading, mutate, isValidating };
};
