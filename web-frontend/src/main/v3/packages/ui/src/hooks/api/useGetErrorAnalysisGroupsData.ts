import useSWR from 'swr';
import { END_POINTS, ErrorAnalysisGroups } from '@pinpoint-fe/ui/src/constants';
import { swrConfigs } from './swrConfigs';

export const useGetErrorAnalysisGroupsData = () => {
  const { data, isLoading, isValidating } = useSWR<ErrorAnalysisGroups.Response>(
    `${END_POINTS.ERROR_ANALYSIS_GROUPS}`,
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
