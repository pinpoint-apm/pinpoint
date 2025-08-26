import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { queryFn } from './reactQueryHelper';

export const useGetApplicationList = (shouldFetch = true) => {
  return useQuery({
    queryKey: [END_POINTS.APPLICATION_LIST],
    queryFn: queryFn(END_POINTS.APPLICATION_LIST),
    enabled: shouldFetch,
  });
};
