import useSWR from 'swr';
import { END_POINTS, InspectorApplicationDataSourceChart } from '@pinpoint-fe/ui/src/constants';
import { swrConfigs } from './swrConfigs';
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

  return useSWR<InspectorApplicationDataSourceChart.Response>(
    queryString ? `${END_POINTS.INSPECTOR_APPLICATION_DATA_SOURCE_CHART}${queryString}` : null,
    swrConfigs,
  );
};
