import { useSuspenseQuery } from '@tanstack/react-query';
import {
  END_POINTS,
  ErrorAnalysisChartType as ErrorAnalysisChart,
} from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useErrorAnalysisSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<ErrorAnalysisChart.Parameters>) => {
  if (queryParams.applicationName && queryParams.from && queryParams.to) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetErrorAnalysisChartData = () => {
  const { application, dateRange, agentId, groupBy } = useErrorAnalysisSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const applicationName = application?.applicationName;
  const queryParams = {
    applicationName,
    from,
    to,
    agentId,
    groupBy,
  };

  const queryString = getQueryString(queryParams);

  const { data, isLoading, isFetching } = useSuspenseQuery<ErrorAnalysisChart.Response | undefined>(
    {
      queryKey: [END_POINTS.ERROR_ANALYSIS_CHART, queryString],
      queryFn: queryString
        ? queryFn(`${END_POINTS.ERROR_ANALYSIS_CHART}${queryString}`)
        : () => undefined,
    },
  );

  return { data, isLoading, isValidating: isFetching };
};
