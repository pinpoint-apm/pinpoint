import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, ErrorAnalysisGroups } from '@pinpoint-fe/ui/src/constants';
import { queryFn } from './reactQueryHelper';

export const useGetErrorAnalysisGroupsData = () => {
  const { data, isLoading, isFetching } = useSuspenseQuery<ErrorAnalysisGroups.Response>({
    queryKey: [END_POINTS.ERROR_ANALYSIS_GROUPS],
    queryFn: queryFn(`${END_POINTS.ERROR_ANALYSIS_GROUPS}`),
  });

  return { data, isLoading, isValidating: isFetching };
};
