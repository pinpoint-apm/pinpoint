import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, ActiveThreadLightDump } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<ActiveThreadLightDump.Parameters>) => {
  if (queryParams.agentId) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export const useGetActiveThreadLightDump = () => {
  const { searchParameters, application } = useSearchParameters();
  const applicationName = application?.applicationName;
  const agentId = searchParameters?.agentId;

  const queryString = getQueryString({
    applicationName,
    agentId,
  });

  const { data, isLoading } = useSuspenseQuery<ActiveThreadLightDump.Response | null>({
    queryKey: [END_POINTS.ACTIVE_THREAD_LIGHT_DUMP, queryString],
    queryFn: !!queryString
      ? queryFn(`${END_POINTS.ACTIVE_THREAD_LIGHT_DUMP}${queryString}`)
      : () => null,
  });

  return { data, isLoading };
};
