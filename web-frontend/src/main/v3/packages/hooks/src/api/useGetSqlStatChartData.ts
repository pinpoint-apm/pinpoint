import React from 'react';
import useSWR from 'swr';
import { END_POINTS, SqlStatChart } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useSqlStatSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

const getQueryString = (queryParams: Partial<SqlStatChart.Parameters>) => {
  if (queryParams.applicationName && queryParams.from && queryParams.to) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetSqlStatChartData = ({
  type,
  selectedQueryList,
}: {
  type: string;
  selectedQueryList: string[];
}) => {
  const { application, dateRange, query, groupBy = 'query' } = useSqlStatSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const [queryParams, setQueryParams] = React.useState<Partial<SqlStatChart.Parameters>>({});

  const queryString = getQueryString(queryParams);
  const generatedQuery = generateQuery(query, selectedQueryList, groupBy);

  React.useEffect(() => {
    setQueryParams((prev) => {
      return {
        ...prev,
        applicationName: application?.applicationName,
        from,
        to,
        type,
        query: prev.groupBy === undefined || prev.groupBy === groupBy ? generatedQuery : query,
        groupBy,
      };
    });
  }, [
    application?.applicationName,
    application?.serviceType,
    from,
    to,
    type,
    query,
    groupBy,
    generatedQuery,
  ]);

  const { data, isLoading, isValidating } = useSWR<SqlStatChart.Response>(
    queryString ? `${END_POINTS.SQL_STATISTIC_CHART}${queryString}` : null,
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};

const generateQuery = (query: string, selectedQueryList: string[], groupBy: string) => {
  let queryList = selectedQueryList;

  if (query) {
    const originalQueryList = query.split(',');

    queryList =
      selectedQueryList.length === 0
        ? originalQueryList
        : [
            ...originalQueryList.filter((query) => !query.startsWith(groupBy)),
            ...selectedQueryList,
          ];
  }

  return queryList.join(',');
};
