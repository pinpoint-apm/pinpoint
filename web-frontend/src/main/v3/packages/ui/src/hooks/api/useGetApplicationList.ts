import useSWR from 'swr';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { swrConfigs } from './swrConfigs';

// https://swr.vercel.app/docs/conditional-fetching
export const useGetApplicationList = (shouldFetch = true) => {
  return useSWR(shouldFetch ? END_POINTS.APPLICATION_LIST : null, swrConfigs);
};
