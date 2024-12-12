import useSWR from 'swr';
import { END_POINTS, UserGroup } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';

export const useGetUserGroup = ({ disableFetch }: { disableFetch?: boolean }) => {
  const { data, isLoading, isValidating } = useSWR<UserGroup.Response>(
    !disableFetch ? [`${END_POINTS.USER_GROUP}`] : null,
    { ...swrConfigs, suspense: false },
  );

  return { data, isLoading, isValidating };
};
