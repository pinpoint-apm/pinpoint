import useSWR from 'swr';
import { END_POINTS, ConfigGroupMember } from '@pinpoint-fe/ui/src/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';

const getQueryString = (queryParams: ConfigGroupMember.Parameters) => {
  if (queryParams.userGroupId) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

export const useGetConfigGroupMember = (params: ConfigGroupMember.Parameters) => {
  const queryString = getQueryString(params);

  return useSWR<ConfigGroupMember.Response>(
    queryString ? `${END_POINTS.CONFIG_GROUP_MEMBER}${queryString}` : null,
    swrConfigs,
  );
};
