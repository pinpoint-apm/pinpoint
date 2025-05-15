import React from 'react';
import { END_POINTS, GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

export const useGetHeatmapAppData = (parameters: GetHeatmapAppData.Parameters) => {
  const queryString = `?${convertParamsToQueryString(parameters)}`;
  const { data, isLoading, refetch, error } = useQuery<GetHeatmapAppData.Response>({
    queryKey: [END_POINTS.HEATMAP_APP_DATA, parameters],
    queryFn: queryFn(`${END_POINTS.HEATMAP_APP_DATA}${queryString}`),
    enabled: !!queryString,
  });
  return { data, isLoading, refetch, error };
};
