import { useQuery } from '@tanstack/react-query';
import { END_POINTS, GetServiceMap } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from './reactQueryHelper';

export const useGetServiceMap = (params: GetServiceMap.Parameters) => {
  const { keepServiceNames, ...rest } = params;
  const queryString = convertParamsToQueryString({
    ...rest,
    keepServiceNames: keepServiceNames?.length ? keepServiceNames.join(',') : 'DEFAULT',
  });

  return useQuery<GetServiceMap.Response>({
    queryKey: [END_POINTS.SERVICE_MAP_DATA, queryString],
    queryFn: queryFn(`${END_POINTS.SERVICE_MAP_DATA}?${queryString}`),
    enabled: !!params.applicationName && !!params.from && !!params.to,
    gcTime: 30000,
  });
};
