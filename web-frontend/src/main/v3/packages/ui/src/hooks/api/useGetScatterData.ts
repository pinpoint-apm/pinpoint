import React from 'react';
import { END_POINTS, GetScatter, ApplicationType } from '@pinpoint-fe/ui/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/utils';
import { useServerMapSearchParameters } from '../searchParameters';
import { useQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (
  queryParams: GetScatter.Parameters & { timestamp?: number },
  applicationName?: string,
) => {
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

export const useGetScatterData = (application: ApplicationType) => {
  const { dateRange } = useServerMapSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  // xGroupUnit, yGroupUnit이 반올림해서 오기 때문에 같을 경우 rerendering을 안함. 그래서 timestamp 를 임시로 받음.
  const [query, setQueryParams] = React.useState<GetScatter.Parameters & { timestamp?: number }>({
    from,
    to,
    application: application?.applicationName,
    limit: 10000,
    filter: '',
    xGroupUnit: undefined,
    yGroupUnit: undefined,
    backwardDirection: true,
    timestamp: undefined,
  });
  const queryParams = React.useDeferredValue(query);
  const queryString = getQueryString(queryParams, application.applicationName);

  React.useEffect(() => {
    setQueryParams((prev) => ({
      ...prev,
      from,
      to,
      application: application?.applicationName,
    }));
  }, [application?.applicationName, from, to]);

  const { data, isLoading } = useQuery({
    queryKey: [END_POINTS.SCATTER_DATA, queryString],
    queryFn: queryFn(`${END_POINTS.SCATTER_DATA}${queryString}`),
    enabled: !!queryString,
    gcTime: 0,
  });

  React.useEffect(() => {
    if (!isLoading && data) {
      if (data?.complete === false) {
        setQueryParams((prev) => {
          // 데이터 페칭 중 사용자 입력으로 인한 api 호출 발생시 충돌
          if (data.resultFrom - (prev.from || 0) > 0) {
            return {
              ...prev,
              to: data.resultFrom - 1,
            };
          }
          return prev;
        });
      }
    }
  }, [isLoading, data]);

  return { data, isLoading, setQueryParams };
};
