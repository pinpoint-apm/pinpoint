import useSWR, { SWRConfiguration } from 'swr';
import { END_POINTS } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';

export const useGetServerTime = (options?: SWRConfiguration) => {
  return useSWR(END_POINTS.SERVER_TIME, { ...swrConfigs, ...options });
};
