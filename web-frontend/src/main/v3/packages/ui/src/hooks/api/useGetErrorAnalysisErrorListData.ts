import useSWR from 'swr';
import { END_POINTS, ErrorAnalysisErrorList } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useErrorAnalysisSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

const getQueryString = (queryParams: Partial<ErrorAnalysisErrorList.Parameters>) => {
  if (queryParams.applicationName && queryParams.from && queryParams.to) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetErrorAnalysisErrorListData = ({
  orderBy,
  isDesc,
  count,
}: {
  orderBy?: string;
  isDesc?: boolean;
  count?: number;
}) => {
  const { application, dateRange, agentId } = useErrorAnalysisSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const applicationName = application?.applicationName;
  const queryParams = {
    applicationName,
    from,
    to,
    agentId,
    isDesc: isDesc ?? true,
    count: count || 50,
    orderBy: orderBy || 'timestamp',
  };
  const queryString = getQueryString(queryParams);

  const { data, isLoading, isValidating } = useSWR<ErrorAnalysisErrorList.Response>(
    [queryString ? `${END_POINTS.ERROR_ANALYSIS_ERROR_LIST}${queryString}` : null],
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
