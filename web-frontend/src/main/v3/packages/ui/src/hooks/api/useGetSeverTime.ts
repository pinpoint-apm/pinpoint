import useSWR, { SWRConfiguration } from 'swr';
import { END_POINTS } from '@pinpoint-fe/ui/constants';
import { swrConfigs } from './swrConfigs';

export const useGetServerTime = (options?: SWRConfiguration) => {
  return useSWR(END_POINTS.SERVER_TIME, { ...swrConfigs, ...options });
};
