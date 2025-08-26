import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, InspectorApplicationChart } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useInspectorSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<InspectorApplicationChart.Parameters>) => {
  if (
    queryParams.applicationName &&
    queryParams.from &&
    queryParams.to &&
    queryParams.metricDefinitionId
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetInspectorApplicationChartData = ({
  metricDefinitionId,
}: {
  metricDefinitionId: string;
}) => {
  const { dateRange, application, version } = useInspectorSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const applicationName = application?.applicationName;
  const serviceTypeName = application?.serviceType;
  const queryParams = {
    applicationName,
    serviceTypeName,
    from,
    to,
    metricDefinitionId,
    version,
  };

  const queryString = getQueryString(queryParams);

  return useSuspenseQuery<InspectorApplicationChart.Response | null>({
    queryKey: [END_POINTS.INSPECTOR_APPLICATION_CHART, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.INSPECTOR_APPLICATION_CHART}${queryString}`)
      : () => null,
  });
};
