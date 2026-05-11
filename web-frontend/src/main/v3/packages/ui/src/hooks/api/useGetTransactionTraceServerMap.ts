import { END_POINTS, TransactionTraceServerMap } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useTransactionSearchParameters } from '../searchParameters';
import { useExperimentals } from '../utility';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const hasLinkParams = (queryParams: Partial<TransactionTraceServerMap.Parameters>) =>
  !!queryParams.linkTraceId && !!queryParams.linkSpanId;

const getQueryString = (queryParams: Partial<TransactionTraceServerMap.Parameters>) => {
  if (hasLinkParams(queryParams)) {
    if (queryParams.spanId && queryParams.traceId) {
      return '?' + convertParamsToQueryString(queryParams);
    }
    return '';
  }
  if (queryParams?.agentId && queryParams?.spanId && queryParams?.traceId) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetTransactionTraceServerMap = () => {
  const { statisticsAgentState } = useExperimentals();
  const { transactionInfo } = useTransactionSearchParameters();

  const queryParams: Partial<TransactionTraceServerMap.Parameters> = {
    agentId: transactionInfo?.agentId,
    spanId: transactionInfo?.spanId,
    traceId: transactionInfo?.traceId,
    focusTimestamp: transactionInfo?.focusTimestamp,
    useStatisticsAgentState: statisticsAgentState.value,
    linkTraceId: transactionInfo?.linkTraceId,
    linkSpanId: transactionInfo?.linkSpanId,
  };

  const queryString = getQueryString(queryParams);
  const endpoint = hasLinkParams(queryParams)
    ? END_POINTS.TRANSACTION_TRACE_SERVER_MAP_LINK
    : END_POINTS.TRANSACTION_TRACE_SERVER_MAP;

  const { data, isLoading, refetch } = useSuspenseQuery<TransactionTraceServerMap.Response | null>({
    queryKey: [endpoint, queryString],
    queryFn: queryFn(`${endpoint}${queryString}`),
  });

  return { data, isLoading, refetch };
};
