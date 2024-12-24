import useSWR from 'swr';
import {
  END_POINTS,
  ErrorAnalysisChartType as ErrorAnalysisChart,
} from '@pinpoint-fe/ui/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';
import { useErrorAnalysisSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

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

  const { data, isLoading, isValidating } = useSWR<ErrorAnalysisChart.Response>(
    [queryString ? `${END_POINTS.ERROR_ANALYSIS_CHART}${queryString}` : null],
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
