import useSWR from 'swr';
import { END_POINTS, ActiveThreadDump, ActiveThreadLightDump } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';
import { useSearchParameters } from '../searchParameters';

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

  const { data, isLoading } = useSWR<ActiveThreadDump.Response>(
    queryString ? [`${END_POINTS.ACTIVE_THREAD_DUMP}${queryString}`] : null,
    swrConfigs,
  );

  return { data, isLoading };
};
