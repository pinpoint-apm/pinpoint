import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { useQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

export const useGetServerTime = () => {
  return useQuery({
    queryKey: [END_POINTS.SERVER_TIME],
    queryFn: queryFn(END_POINTS.SERVER_TIME),
  });
};
