import { END_POINTS, SystemMetricHost } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useSystemMetricSearchParameters } from '../searchParameters';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<SystemMetricHost.Parameters>) => {
  if (queryParams.hostGroupName) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetSystemMetricHostData = () => {
  const { hostGroupName } = useSystemMetricSearchParameters();
  const queryParams = {
    hostGroupName,
  };

  const queryString = getQueryString(queryParams);

  const { data, isLoading, isFetching } = useSuspenseQuery<SystemMetricHost.Response | null>({
    queryKey: [END_POINTS.SYSTEM_METRIC_HOST, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.SYSTEM_METRIC_HOST}${queryString}`) : () => null,
  });

  return { data, isLoading, isValidating: isFetching };
};
