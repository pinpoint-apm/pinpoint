import { useQuery } from '@tanstack/react-query';
import { END_POINTS, TransactionTraceMetadata } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

// Shared by the hook below and click-time queryClient.fetchQuery callers
// (e.g. the OTel Link jump) so both hit the same cache entry.
export const getTransactionTraceMetadataQueryOptions = (traceId?: string) => {
  const queryString = traceId ? `?${convertParamsToQueryString({ traceId })}` : '';
  return {
    queryKey: [END_POINTS.TRANSACTION_TRACE_METADATA, queryString] as const,
    queryFn: queryFn(
      `${END_POINTS.TRANSACTION_TRACE_METADATA}${queryString}`,
    ) as () => Promise<TransactionTraceMetadata.Response>,
  };
};

export const useGetTransactionTraceMetadata = (
  params: Partial<TransactionTraceMetadata.Parameters>,
) => {
  return useQuery<TransactionTraceMetadata.Response>({
    ...getTransactionTraceMetadataQueryOptions(params.traceId),
    enabled: !!params.traceId,
    gcTime: 30000,
  });
};
