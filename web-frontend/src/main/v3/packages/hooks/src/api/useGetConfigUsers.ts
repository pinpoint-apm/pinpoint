import useSWR from 'swr';
import { ConfigUsers, END_POINTS } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';

const getQueryString = (queryParams?: Partial<ConfigUsers.Parameters>) => {
  if (queryParams?.searchKey) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

export const useGetConfigUsers = (params?: ConfigUsers.Parameters) => {
  const queryString = getQueryString(params);

  return useSWR<ConfigUsers.Response>(
    queryString ? `${END_POINTS.CONFIG_USERS}${queryString}` : null,
    swrConfigs,
  );
};
