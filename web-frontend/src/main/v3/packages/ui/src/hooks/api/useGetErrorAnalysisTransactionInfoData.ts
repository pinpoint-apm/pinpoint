import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, ErrorAnalysisTransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<ErrorAnalysisTransactionInfo.Parameters>) => {
  if (
    queryParams.applicationName &&
    queryParams.agentId &&
    queryParams.transactionId &&
    queryParams.spanId &&
    queryParams.exceptionId
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetErrorAnalysisTransactionInfoData = ({
  applicationName,
  agentId,
  transactionId,
  spanId,
  exceptionId,
}: {
  applicationName: string;
  agentId: string;
  transactionId: string;
  spanId: number;
  exceptionId: number;
}) => {
  const queryString = getQueryString({
    applicationName,
    agentId,
    transactionId,
    spanId,
    exceptionId,
  });

  const { data, isLoading, isFetching } = useSuspenseQuery<
    ErrorAnalysisTransactionInfo.Response | undefined
  >({
    queryKey: [END_POINTS.ERROR_ANALYSIS_TRANSACTION_INFO, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.ERROR_ANALYSIS_TRANSACTION_INFO}${queryString}`)
      : () => undefined,
  });

  return { data, isLoading, isValidating: isFetching };
};
