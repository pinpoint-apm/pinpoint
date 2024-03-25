import useSWR from 'swr';
import { END_POINTS, SqlStatGroups } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';

export const useGetSqlStatGroupsData = () => {
  const { data, isLoading, isValidating } = useSWR<SqlStatGroups.Response>(
    `${END_POINTS.SQL_STATISTIC_GROUPS}`,
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
