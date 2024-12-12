import useSWR, { SWRConfiguration } from 'swr';
import { END_POINTS } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';

export const useGetConfiguration = <T>(options?: SWRConfiguration) => {
  return useSWR<T>(END_POINTS.CONFIGURATION, { ...swrConfigs, ...options });
};
