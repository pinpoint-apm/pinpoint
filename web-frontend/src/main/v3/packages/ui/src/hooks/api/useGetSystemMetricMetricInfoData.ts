import useSWR from 'swr';
import { END_POINTS, SystemMetricMetricInfo } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useSystemMetricSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

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

  const { data, isLoading, isValidating } = useSWR<SystemMetricMetricInfo.Response>(
    queryString ? `${END_POINTS.SYSTEM_METRIC_METRIC_INFO}${queryString}` : null,
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
