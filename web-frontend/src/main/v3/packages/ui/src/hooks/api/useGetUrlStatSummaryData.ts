import useSWR from 'swr';
import { END_POINTS, UrlStatSummary } from '@pinpoint-fe/ui/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';
import { useUrlStatSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

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
}: {
  orderBy?: string;
  isDesc?: boolean;
  count?: number;
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
    // orderBy: orderBy || 'totalCount',
    orderby: orderBy || 'totalCount', // * url 통계 쪽 api에선 파라미터를 orderby로 쓰고있음
  };
  const queryString = getQueryString(queryParams);

  const { data, isLoading, isValidating } = useSWR<UrlStatSummary.Response>(
    [queryString ? `${END_POINTS.URL_STATISTIC_SUMMARY}${queryString}` : null],
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
