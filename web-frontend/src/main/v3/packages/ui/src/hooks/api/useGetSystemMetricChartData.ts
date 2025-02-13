import useSWR from 'swr';
import { END_POINTS, SystemMetricChart } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useSystemMetricSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

const getQueryString = (queryParams: Partial<SystemMetricChart.Parameters>) => {
  if (
    queryParams.hostGroupName &&
    queryParams.hostName &&
    queryParams.metricDefinitionId &&
    queryParams.from &&
    queryParams.to
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetSystemMetricChartData = ({
  metricDefinitionId,
  tags,
}: {
  metricDefinitionId: string;
  tags?: string;
}) => {
  const { hostGroupName, hostName, dateRange } = useSystemMetricSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const queryParams = {
    hostGroupName,
    hostName,
    metricDefinitionId,
    from,
    to,
    tags,
  };

  const queryString = getQueryString(queryParams);

  const { data, isLoading, isValidating } = useSWR<SystemMetricChart.Response>(
    queryString ? `${END_POINTS.SYSTEM_METRIC_CHART}${queryString}` : null,
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
