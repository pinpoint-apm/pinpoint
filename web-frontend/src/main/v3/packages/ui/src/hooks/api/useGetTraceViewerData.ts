import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, TraceViewerData } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<TraceViewerData.Parameters>) => {
  if (queryParams.traceId && queryParams.agentId && queryParams.spanId) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetTraceViewerData = (queryParams: TraceViewerData.Parameters) => {
  const queryString = getQueryString(queryParams);

  const { data, isLoading, isFetching } = useSuspenseQuery<TraceViewerData.Response>({
    queryKey: [END_POINTS.TRACE_VIEWER_DATA, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.TRACE_VIEWER_DATA}${queryString}`)
      : async () => null,
  });

  return { data, isLoading, isValidating: isFetching };
};
