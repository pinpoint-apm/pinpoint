import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, ErrorAnalysisErrorList } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useErrorAnalysisSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

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

  const { data, isLoading, isFetching } = useSuspenseQuery<ErrorAnalysisErrorList.Response | null>({
    queryKey: [END_POINTS.ERROR_ANALYSIS_ERROR_LIST, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.ERROR_ANALYSIS_ERROR_LIST}${queryString}`)
      : () => null,
  });

  return { data, isLoading, isValidating: isFetching };
};
