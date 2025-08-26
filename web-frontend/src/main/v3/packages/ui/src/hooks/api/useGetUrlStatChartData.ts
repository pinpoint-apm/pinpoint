import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, UrlStatChartType as UrlStatChart } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useUrlStatSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

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

  const { data, isLoading, isFetching } = useSuspenseQuery<UrlStatChart.Response | undefined>({
    queryKey: [END_POINTS.URL_STATISTIC_CHART, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.URL_STATISTIC_CHART}${queryString}`)
      : () => undefined,
  });

  return { data, isLoading, isValidating: isFetching };
};
