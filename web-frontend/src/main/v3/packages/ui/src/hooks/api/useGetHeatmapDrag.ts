import { END_POINTS, HeatmapDrag } from '@pinpoint-fe/ui/src/constants';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useTransactionSearchParameters } from '../searchParameters/useTransactionSearchParameters';

const getQueryString = (queryParams: Partial<HeatmapDrag.Parameters>) => {
  if (queryParams.application) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetHeatmapDrag = (params?: { x2?: number }) => {
  const { application, dragInfo } = useTransactionSearchParameters();

  const queryParams = {
    x1: dragInfo?.x1,
    x2: params?.x2 || dragInfo?.x2,
    y1: dragInfo?.y1,
    y2: dragInfo?.y2,
    application: application?.applicationName,
    agentId: dragInfo?.agentId ? dragInfo?.agentId : undefined,
    dotStatus:
      dragInfo?.dotStatus?.length === 1
        ? dragInfo.dotStatus[0] === 'failed'
          ? false
          : true
        : undefined,
  };
  const queryString = getQueryString(queryParams);

  return useSuspenseQuery<HeatmapDrag.Response>({
    queryKey: [END_POINTS.HEATMAP_DRAG, queryString],
    queryFn: queryString ? queryFn(`${END_POINTS.HEATMAP_DRAG}${queryString}`) : async () => null,
  });
};
