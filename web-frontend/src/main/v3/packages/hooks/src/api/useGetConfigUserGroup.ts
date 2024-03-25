import useSWR from 'swr';
import { END_POINTS, ConfigUserGroup } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';

const getQueryString = (queryParams: ConfigUserGroup.Parameters) => {
  if ('userId' in queryParams || queryParams.userGroupId) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

export const useGetConfigUserGroup = (params: ConfigUserGroup.Parameters) => {
  const queryString = getQueryString(params);

  return useSWR<ConfigUserGroup.Response>(
    queryString ? `${END_POINTS.CONFIG_USER_GROUP}${queryString}` : null,
    swrConfigs,
  );
};
