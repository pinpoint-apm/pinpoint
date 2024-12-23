import useSWR from 'swr';
import { END_POINTS, UrlStatChart } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';
import { useUrlStatSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

const getQueryString = (queryParams: Partial<UrlStatChart.Parameters>) => {
  if (queryParams.applicationName && queryParams.from && queryParams.to && queryParams.uri) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetUrlStatChartData = ({ type, uri = '' }: { type: string; uri: string }) => {
  const { application, dateRange, agentId } = useUrlStatSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const applicationName = application?.applicationName;
  const queryParams = {
    applicationName,
    from,
    to,
    type,
    uri,
    agentId,
  };

  const queryString = getQueryString(queryParams);

  const { data, isLoading, isValidating } = useSWR<UrlStatChart.Response>(
    queryString ? `${END_POINTS.URL_STATISTIC_CHART}${queryString}` : null,
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
