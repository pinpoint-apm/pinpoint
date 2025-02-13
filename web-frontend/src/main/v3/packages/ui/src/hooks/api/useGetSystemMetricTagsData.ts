import useSWR from 'swr';
import { END_POINTS, SystemMetricTags } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useSystemMetricSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

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

  const { data, isLoading, isValidating } = useSWR<SystemMetricTags.Response>(
    queryString ? `${END_POINTS.SYSTEM_METRIC_TAGS}${queryString}` : null,
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
