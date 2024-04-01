import useSWR from 'swr';
import { END_POINTS, ActiveThreadLightDump } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useSearchParameters } from '../searchParameters';

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

  const { data, isLoading } = useSWR<ActiveThreadLightDump.Response>(
    queryString ? [`${END_POINTS.ACTIVE_THREAD_LIGHT_DUMP}${queryString}`] : null,
    swrConfigs,
  );

  return { data, isLoading };
};
