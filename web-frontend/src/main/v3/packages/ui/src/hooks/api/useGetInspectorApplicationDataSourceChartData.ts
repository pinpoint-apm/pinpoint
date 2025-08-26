import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, InspectorApplicationDataSourceChart } from '@pinpoint-fe/ui/src/constants';
import { queryFn } from './reactQueryHelper';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useInspectorSearchParameters } from '../searchParameters';

const getQueryString = (queryParams: Partial<InspectorApplicationDataSourceChart.Parameters>) => {
  if (
    queryParams.applicationName &&
    queryParams.from &&
    queryParams.to &&
    queryParams.metricDefinitionId
  ) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

type DataSourceMetricDefinitionId = 'dataSource';

export const useGetInspectorApplicationDataSourceChartData = ({
  metricDefinitionId,
}: {
  metricDefinitionId: DataSourceMetricDefinitionId;
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

  return useSuspenseQuery<InspectorApplicationDataSourceChart.Response | null>({
    queryKey: [END_POINTS.INSPECTOR_APPLICATION_DATA_SOURCE_CHART, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.INSPECTOR_APPLICATION_DATA_SOURCE_CHART}${queryString}`)
      : () => null,
  });
};
