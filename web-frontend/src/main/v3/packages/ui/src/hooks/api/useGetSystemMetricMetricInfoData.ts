import { END_POINTS, SystemMetricMetricInfo } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useSystemMetricSearchParameters } from '../searchParameters';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<SystemMetricMetricInfo.Parameters>) => {
  if (queryParams.hostGroupName && queryParams.hostName) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetSystemMetricMetricInfoData = () => {
  const { hostGroupName, hostName } = useSystemMetricSearchParameters();
  const queryParams = {
    hostGroupName,
    hostName,
  };

  const queryString = getQueryString(queryParams);

  const { data, isLoading, isFetching } = useSuspenseQuery<SystemMetricMetricInfo.Response | null>({
    queryKey: [END_POINTS.SYSTEM_METRIC_METRIC_INFO, queryString],
    queryFn: !!queryString
      ? queryFn(`${END_POINTS.SYSTEM_METRIC_METRIC_INFO}${queryString}`)
      : () => null,
  });

  return { data, isLoading, isValidating: isFetching };
};
