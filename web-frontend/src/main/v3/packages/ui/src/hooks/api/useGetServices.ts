import { useQuery } from '@tanstack/react-query';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import { END_POINTS, GetServices } from '@pinpoint-fe/ui/src/constants';
import { queryFn } from './reactQueryHelper';

export const useGetServices = ({ enabled = true }: { enabled?: boolean } = {}) => {
  return useQuery<GetServices.Response>({
    queryKey: [END_POINTS.SERVICES],
    queryFn: queryFn(END_POINTS.SERVICES),
    enabled,
    select: (data) => (data.includes(DEFAULT_SERVICE) ? data : [DEFAULT_SERVICE, ...data]),
  });
};
