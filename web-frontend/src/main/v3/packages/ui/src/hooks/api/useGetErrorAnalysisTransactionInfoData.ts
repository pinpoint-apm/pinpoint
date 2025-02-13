import useSWR from 'swr';
import { END_POINTS, ErrorAnalysisTransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { swrConfigs } from './swrConfigs';

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

  const { data, isLoading, isValidating } = useSWR<ErrorAnalysisTransactionInfo.Response>(
    [queryString ? `${END_POINTS.ERROR_ANALYSIS_TRANSACTION_INFO}${queryString}` : null],
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
