import { useQuery } from '@tanstack/react-query';
import { END_POINTS, SearchApplication } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';
import { queryFn } from './reactQueryHelper';

export enum AGENT_LIST_SORT_BY {
  ID = 'AGENT_ID_ASC',
  NAME = 'AGENT_NAME_ASC',
  RECENT = 'RECENT',
}

export const useGetAgentsSearchApplication = (params: SearchApplication.Parameters) => {
  const queryString = convertParamsToQueryString(params);

  const { data, isLoading, refetch } = useQuery<SearchApplication.Response>({
    queryKey: [END_POINTS.SEARCH_APPLICATION, params],
    queryFn: queryFn(`${END_POINTS.SEARCH_APPLICATION}?${queryString}`),
    enabled: !!params?.application,
  });

  return { data, isLoading, refetch };
};
