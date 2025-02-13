import useSWR from 'swr';
import { END_POINTS, UserGroupType as UserGroup } from '@pinpoint-fe/ui/src/constants';
import { swrConfigs } from './swrConfigs';

export const useGetUserGroup = ({ disableFetch }: { disableFetch?: boolean }) => {
  const { data, isLoading, isValidating } = useSWR<UserGroup.Response>(
    !disableFetch ? [`${END_POINTS.USER_GROUP}`] : null,
    { ...swrConfigs, suspense: false },
  );

  return { data, isLoading, isValidating };
};
