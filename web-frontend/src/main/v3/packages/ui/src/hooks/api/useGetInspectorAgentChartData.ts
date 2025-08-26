import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, InspectorAgentChart } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useInspectorSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<InspectorAgentChart.Parameters>) => {
  if (queryParams.agentId && queryParams.from && queryParams.to && queryParams.metricDefinitionId) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetInspectorAgentChartData = ({
  metricDefinitionId,
  fromDate,
  toDate,
  agentId,
}: {
  metricDefinitionId: string;
  fromDate?: Date;
  toDate?: Date;
  agentId?: string;
}) => {
  const {
    dateRange,
    agentId: agentIdFromSearchParam,
    application,
    version,
  } = useInspectorSearchParameters();
  const applicationName = application?.applicationName;
  const serviceTypeName = application?.serviceType;
  const from = fromDate?.getTime() || dateRange.from.getTime();
  const to = toDate?.getTime() || dateRange.to.getTime();
  const queryParams = {
    applicationName,
    serviceTypeName,
    agentId: agentId || agentIdFromSearchParam,
    from,
    to,
    metricDefinitionId,
    version,
  };

  const queryString = getQueryString(queryParams);

  return useSuspenseQuery<InspectorAgentChart.Response | null>({
    queryKey: [END_POINTS.INSPECTOR_AGENT_CHART, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.INSPECTOR_AGENT_CHART}${queryString}`)
      : () => null,
  });
};
