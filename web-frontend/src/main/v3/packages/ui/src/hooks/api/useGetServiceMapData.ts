import { GetServiceMap, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<GetServiceMap.Parameters>) => {
  if (
    queryParams.useStatisticsAgentState !== null &&
    queryParams.useStatisticsAgentState !== undefined &&
    queryParams.applicationName &&
    queryParams.serviceTypeName &&
    queryParams.from &&
    queryParams.to
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetServiceMapData = (params: Partial<GetServiceMap.Parameters>) => {
  const queryString = getQueryString(params);

  return useQuery<GetServiceMap.Response>({
    queryKey: [END_POINTS.SERVICE_MAP_DATA, queryString],
    queryFn: queryFn(`${END_POINTS.SERVICE_MAP_DATA}${queryString}`),
    enabled: !!queryString,
    gcTime: 30000,
  });
};
