import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, TraceViewerData } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

const hasLinkParams = (queryParams: Partial<TraceViewerData.Parameters>) =>
  !!queryParams.linkTraceId && !!queryParams.linkSpanId;

const getQueryString = (queryParams: Partial<TraceViewerData.Parameters>) => {
  if (hasLinkParams(queryParams)) {
    if (queryParams.traceId && queryParams.spanId) {
      return '?' + convertParamsToQueryString(queryParams);
    }
    return '';
  }
  if (queryParams.traceId && queryParams.agentId && queryParams.spanId) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetTraceViewerData = (queryParams: TraceViewerData.Parameters) => {
  const queryString = getQueryString(queryParams);
  const endpoint = hasLinkParams(queryParams)
    ? END_POINTS.TRACE_VIEWER_DATA_LINK
    : END_POINTS.TRACE_VIEWER_DATA;

  const { data, isLoading, isFetching } = useSuspenseQuery<TraceViewerData.Response | null>({
    queryKey: [endpoint, queryString],
    queryFn: queryString ? queryFn(`${endpoint}${queryString}`) : async () => null,
  });

  return { data, isLoading, isValidating: isFetching };
};
