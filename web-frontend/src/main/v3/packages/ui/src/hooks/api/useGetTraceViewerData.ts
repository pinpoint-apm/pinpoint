import useSWR from 'swr';
import { END_POINTS, TraceViewerData } from '@pinpoint-fe/ui/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';
import { swrConfigs } from './swrConfigs';

const getQueryString = (queryParams: Partial<TraceViewerData.Parameters>) => {
  if (queryParams.traceId && queryParams.agentId && queryParams.spanId) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetTraceViewerData = (queryParams: TraceViewerData.Parameters) => {
  const queryString = getQueryString(queryParams);

  const { data, isLoading, isValidating } = useSWR<TraceViewerData.Response>(
    queryString ? `${END_POINTS.TRACE_VIEWER_DATA}${queryString}` : null,
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
