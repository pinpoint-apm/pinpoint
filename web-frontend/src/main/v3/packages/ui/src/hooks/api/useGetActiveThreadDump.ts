import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, ActiveThreadDump, ActiveThreadLightDump } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (
  queryParams: Partial<ActiveThreadDump.Parameters> & Partial<ActiveThreadLightDump.ThreadDumpData>,
) => {
  if (
    queryParams?.agentId &&
    queryParams?.applicationName &&
    queryParams?.threadName &&
    queryParams?.localTraceId
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export const useGetActiveThreadDump = (thread?: ActiveThreadLightDump.ThreadDumpData) => {
  const { searchParameters, application } = useSearchParameters();
  const applicationName = application?.applicationName;
  const agentId = searchParameters?.agentId;

  const queryString = getQueryString({
    applicationName,
    agentId,
    threadName: thread?.threadName,
    localTraceId: thread?.localTraceId,
  });

  const { data, isLoading } = useSuspenseQuery<ActiveThreadDump.Response | null>({
    queryKey: [END_POINTS.ACTIVE_THREAD_DUMP, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.ACTIVE_THREAD_DUMP}${queryString}`) : () => null,
  });

  return { data, isLoading };
};
