import React from 'react';
import useSWR from 'swr';
import { END_POINTS, SqlStatSummary } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useSqlStatSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

const getQueryString = (queryParams: Partial<SqlStatSummary.Parameters>) => {
  if (queryParams.applicationName && queryParams.from && queryParams.to) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetSqlStatSummaryData = ({
  orderBy,
  isDesc,
  count = 50,
}: {
  orderBy?: string;
  isDesc?: boolean;
  count?: number;
}) => {
  const { application, dateRange, query, groupBy } = useSqlStatSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const applicationName = application?.applicationName;
  const [queryParams, setQueryParams] = React.useState<Partial<SqlStatSummary.Parameters>>({
    applicationName,
    from,
    to,
    query,
    orderBy,
    // TODO: 향후 빈스트링 넣어도 BE에서 query로 처리
    groupBy: groupBy ? groupBy : 'query',
    isDesc,
    count,
  });

  const queryString = getQueryString(queryParams);

  React.useEffect(() => {
    setQueryParams((prev) => ({
      ...prev,
      applicationName: application?.applicationName,
      from,
      to,
      query,
      orderBy,
      groupBy: groupBy ? groupBy : 'query',
      isDesc,
      count,
    }));
  }, [
    application?.applicationName,
    application?.serviceType,
    from,
    to,
    query,
    orderBy,
    groupBy,
    isDesc,
    count,
  ]);

  const { data, isLoading, isValidating } = useSWR<SqlStatSummary.Response>(
    [queryString ? `${END_POINTS.SQL_STATISTIC_SUMMARY}${queryString}` : null],
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
