import useSWR from 'swr';
import { END_POINTS, SystemMetricHost } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useSystemMetricSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

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

  const { data, isLoading, isValidating } = useSWR<SystemMetricHost.Response>(
    queryString ? `${END_POINTS.SYSTEM_METRIC_HOST}${queryString}` : null,
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
