import React from 'react';
import { ApplicationType, END_POINTS, GetScatter } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: GetScatter.Parameters, applicationName?: string) => {
  if (
    queryParams.from &&
    queryParams.to &&
    queryParams.xGroupUnit &&
    queryParams.yGroupUnit &&
    applicationName
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export const useGetScatterRealtimeData = (
  application: ApplicationType,
  dateRange: {
    from: Date;
    to: Date;
    isRealtime: boolean;
  },
) => {
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();

  // xGroupUnit, yGroupUnit이 반올림해서 오기 때문에 같을 경우 rerendering을 안함. 그래서 timestamp 를 임시로 받음.
  const [queryParams, setQueryParams] = React.useState<
    GetScatter.Parameters & { timestamp?: number }
  >({
    from: 0,
    to: 0,
    application: application?.applicationName,
    limit: 10000,
    filter: '',
    xGroupUnit: undefined,
    yGroupUnit: undefined,
    backwardDirection: true,
    timestamp: undefined,
  });
  const queryString = getQueryString(queryParams, application.applicationName);

  const { data, isLoading } = useQuery({
    queryKey: [END_POINTS.SCATTER_DATA, queryString],
    queryFn: queryFn(`${END_POINTS.SCATTER_DATA}${queryString}`),
    enabled: !!queryString,
    gcTime: 0,
  });

  React.useEffect(() => {
    setQueryParams((prev) => ({
      ...prev,
      from,
      to,
      application: application.applicationName,
    }));
  }, [application.applicationName]);

  React.useEffect(() => {
    if (!isLoading && data) {
      if (!data?.complete) {
        setQueryParams((prev) => ({
          ...prev,
          to: data.resultFrom - 1,
        }));
      }
    }
  }, [data]);

  React.useEffect(() => {
    setQueryParams((prev) => ({
      ...prev,
      from,
      to,
    }));
  }, [from, to]);

  return { data, isLoading, setQueryParams };
};
