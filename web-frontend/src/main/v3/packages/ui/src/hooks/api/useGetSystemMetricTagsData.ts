import { END_POINTS, SystemMetricTags } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useSystemMetricSearchParameters } from '../searchParameters';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<SystemMetricTags.Parameters>) => {
  if (queryParams.hostGroupName && queryParams.hostName && queryParams.metricDefinitionId) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetSystemMetricTagsData = ({
  metricDefinitionId,
}: {
  metricDefinitionId: string;
}) => {
  const { hostGroupName, hostName } = useSystemMetricSearchParameters();
  const queryParams = {
    hostGroupName,
    hostName,
    metricDefinitionId,
  };

  const queryString = getQueryString(queryParams);

  const { data, isLoading, isFetching } = useSuspenseQuery<SystemMetricTags.Response | null>({
    queryKey: [END_POINTS.SYSTEM_METRIC_TAGS, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.SYSTEM_METRIC_TAGS}${queryString}`) : () => null,
  });

  return { data, isLoading, isValidating: isFetching };
};
