import { END_POINTS, TransactionTraceServerMap } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useTransactionSearchParameters } from '../searchParameters';
import { useExperimentals } from '../utility';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<TransactionTraceServerMap.Parameters>) => {
  if (queryParams?.agentId && queryParams?.spanId && queryParams?.traceId) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetTransactionTraceServerMap = () => {
  const { statisticsAgentState } = useExperimentals();
  const { transactionInfo } = useTransactionSearchParameters();

  const queryParams = {
    agentId: transactionInfo?.agentId,
    spanId: transactionInfo?.spanId,
    traceId: transactionInfo?.traceId,
    focusTimestamp: transactionInfo?.focusTimestamp,
    useStatisticsAgentState: statisticsAgentState.value,
  };

  const queryString = getQueryString(queryParams);

  const { data, isLoading, refetch } = useSuspenseQuery<TransactionTraceServerMap.Response | null>({
    queryKey: [END_POINTS.TRANSACTION_TRACE_SERVER_MAP, queryString],
    queryFn: queryFn(`${END_POINTS.TRANSACTION_TRACE_SERVER_MAP}${queryString}`),
  });

  return { data, isLoading, refetch };
};
