import React from 'react';
import useSWR from 'swr';
import { END_POINTS, SqlStatFilterOptions } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useSqlStatSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

const getQueryString = (queryParams: Partial<SqlStatFilterOptions.Parameters>) => {
  if (queryParams.applicationName && queryParams.from && queryParams.to) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetSqlStatFilterOptionsData = () => {
  const { application, dateRange } = useSqlStatSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const applicationName = application?.applicationName;
  const [queryParams, setQueryParams] = React.useState<Partial<SqlStatFilterOptions.Parameters>>({
    applicationName,
    from,
    to,
  });

  const queryString = getQueryString(queryParams);

  React.useEffect(() => {
    setQueryParams((prev) => ({
      ...prev,
      applicationName: application?.applicationName,
      from,
      to,
    }));
  }, [application?.applicationName, application?.serviceType, from, to]);

  const { data, isLoading, isValidating } = useSWR<SqlStatFilterOptions.Response>(
    [queryString ? `${END_POINTS.SQL_STATISTIC_FILTER_OPTIONS}${queryString}` : null],
    {
      ...swrConfigs,
      keepPreviousData: true,
    },
  );

  return { data, isLoading, isValidating };
};
