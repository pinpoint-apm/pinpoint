import useSWR, { SWRConfiguration } from 'swr';
import { END_POINTS, HeatmapDrag } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';
import { useTransactionSearchParameters } from '../searchParameters/useTransactionSearchParameters';

const getQueryString = (queryParams: Partial<HeatmapDrag.Parameters>) => {
  if (queryParams.application) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetHeatmapDrag = (params?: { x2?: number }, options?: SWRConfiguration) => {
  const { application, dragInfo } = useTransactionSearchParameters();

  const queryParams = {
    x1: dragInfo.x1,
    x2: params?.x2 || dragInfo.x2,
    y1: dragInfo.y1,
    y2: dragInfo.y2,
    application: application?.applicationName,
    agentId: dragInfo.agentId ? dragInfo.agentId : undefined,
    dotStatus:
      dragInfo?.dotStatus?.length === 1
        ? dragInfo.dotStatus[0] === 'failed'
          ? false
          : true
        : undefined,
  };
  const queryString = getQueryString(queryParams);

  return useSWR(queryString ? `${END_POINTS.HEATMAP_DRAG}${queryString}` : null, {
    ...swrConfigs,
    ...options,
  });
};
