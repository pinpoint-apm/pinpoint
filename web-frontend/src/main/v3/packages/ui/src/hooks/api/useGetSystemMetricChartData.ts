import { END_POINTS, SystemMetricChart } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString, toBasicISOString } from '@pinpoint-fe/ui/src/utils';
import { useSystemMetricSearchParameters } from '../searchParameters';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<SystemMetricChart.Parameters>, tagGroup?: boolean) => {
  if (
    queryParams.hostGroupName &&
    queryParams.hostName &&
    queryParams.metricDefinitionId &&
    queryParams.from &&
    queryParams.to &&
    // tagGroup이 있는 metric은 tags가 정해지기 전에는 요청하지 않는다 (빈 tags= 요청 방지)
    (!tagGroup || queryParams.tags)
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetSystemMetricChartData = ({
  metricDefinitionId,
  tagGroup,
  tags,
}: {
  metricDefinitionId: string;
  tagGroup?: boolean;
  tags?: string;
}) => {
  const { hostGroupName, hostName, dateRange } = useSystemMetricSearchParameters();
  const from = toBasicISOString(dateRange.from);
  const to = toBasicISOString(dateRange.to);
  const queryParams = {
    hostGroupName,
    hostName,
    metricDefinitionId,
    from,
    to,
    tags,
  };

  const queryString = getQueryString(queryParams, tagGroup);

  const { data, isLoading, isFetching } = useSuspenseQuery<SystemMetricChart.Response | null>({
    queryKey: [END_POINTS.SYSTEM_METRIC_CHART, queryString],
    queryFn: !!queryString
      ? queryFn(`${END_POINTS.SYSTEM_METRIC_CHART}${queryString}`)
      : () => null,
  });

  return { data, isLoading, isValidating: isFetching };
};
