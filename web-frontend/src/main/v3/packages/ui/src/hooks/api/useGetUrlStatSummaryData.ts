import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, UrlStatSummary } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useUrlStatSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<UrlStatSummary.Parameters>) => {
  if (queryParams.applicationName && queryParams.from && queryParams.to) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetUrlStatSummaryData = ({
  orderBy,
  isDesc,
  count,
  type,
}: {
  orderBy?: string;
  isDesc?: boolean;
  count?: number;
  type?: UrlStatSummary.Parameters['type'];
}) => {
  const { application, dateRange, agentId } = useUrlStatSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const applicationName = application?.applicationName;
  const queryParams = {
    applicationName,
    from,
    to,
    agentId,
    isDesc: isDesc ?? true,
    count: count || 50,
    type,
    orderby: orderBy || 'totalCount', // * url 통계 쪽 api에선 파라미터를 orderby로 쓰고있음
  };
  const queryString = getQueryString(queryParams);

  const { data, isLoading, isFetching } = useSuspenseQuery<UrlStatSummary.Response | null>({
    queryKey: [END_POINTS.URL_STATISTIC_SUMMARY, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.URL_STATISTIC_SUMMARY}${queryString}`)
      : () => null,
  });

  return { data, isLoading, isValidating: isFetching };
};
