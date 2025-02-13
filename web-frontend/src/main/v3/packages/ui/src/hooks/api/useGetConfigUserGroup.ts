import useSWR from 'swr';
import { END_POINTS, ConfigUserGroup } from '@pinpoint-fe/ui/src/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString, isEmpty } from '@pinpoint-fe/ui/src/utils';

const getQueryString = (queryParams: ConfigUserGroup.Parameters) => {
  if (isEmpty(queryParams)) {
    return '';
  }

  return `?${convertParamsToQueryString(queryParams)}`;
};

export const useGetConfigUserGroup = (params: ConfigUserGroup.Parameters) => {
  const queryString = getQueryString(params);

  return useSWR<ConfigUserGroup.Response>(
    `${END_POINTS.CONFIG_USER_GROUP}${queryString}`,
    swrConfigs,
  );
};
