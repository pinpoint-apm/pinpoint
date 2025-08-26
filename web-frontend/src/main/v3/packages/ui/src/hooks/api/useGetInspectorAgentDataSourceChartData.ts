import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, InspectorAgentDataSourceChart } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useInspectorSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<InspectorAgentDataSourceChart.Parameters>) => {
  if (queryParams.agentId && queryParams.from && queryParams.to && queryParams.metricDefinitionId) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

type DataSourceMetricDefinitionId = 'dataSource';

export const useGetInspectorAgentDataSourceChartData = ({
  metricDefinitionId,
}: {
  metricDefinitionId: DataSourceMetricDefinitionId;
}) => {
  const { dateRange, agentId, application, version } = useInspectorSearchParameters();
  const applicationName = application?.applicationName;
  const serviceTypeName = application?.serviceType;
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const queryParams = {
    applicationName,
    serviceTypeName,
    agentId,
    from,
    to,
    metricDefinitionId,
    version,
  };

  const queryString = getQueryString(queryParams);

  return useSuspenseQuery<InspectorAgentDataSourceChart.Response | null>({
    queryKey: [END_POINTS.INSPECTOR_AGENT_DATA_SOURCE_CHART, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.INSPECTOR_AGENT_DATA_SOURCE_CHART}${queryString}`)
      : () => null,
  });
};
