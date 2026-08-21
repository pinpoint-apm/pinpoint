import React from 'react';
import { END_POINTS, GetScatter, ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString, toBasicISOStringMs } from '@pinpoint-fe/ui/src/utils';
import { useQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: GetScatter.Parameters & { timestamp?: number }) => {
  if (
    queryParams.from &&
    queryParams.to &&
    queryParams.xGroupUnit &&
    queryParams.yGroupUnit &&
    queryParams?.application &&
    queryParams?.serviceTypeName
  ) {
    return (
      '?' +
      convertParamsToQueryString({
        ...queryParams,
        from: toBasicISOStringMs(new Date(queryParams.from)),
        to: toBasicISOStringMs(new Date(queryParams.to)),
      })
    );
  }

  return '';
};

/**
 * @param serviceName 이 조회가 나갈 service. 지정하면 `pServiceName` 헤더를 직접 싣고 캐시도 그
 * 단위로 갈린다. 화면의 service와 조회 대상의 service가 다를 때 넘긴다 — servicemap은 다른
 * service의 application도 함께 그리기 때문이다(`useServerMapTargetServiceName`). 넘기지 않으면
 * 기존대로 fetch 인터셉터가 경로/전역 선택값으로 결정한다.
 */
export const useGetScatterData = (
  application: ApplicationType,
  dateRange: {
    isRealtime: boolean;
    from: Date;
    to: Date;
  },
  serviceName?: string,
) => {
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  // xGroupUnit, yGroupUnit이 반올림해서 오기 때문에 같을 경우 rerendering을 안함. 그래서 timestamp 를 임시로 받음.
  const [query, setQueryParams] = React.useState<GetScatter.Parameters & { timestamp?: number }>({
    from,
    to,
    application: application?.applicationName,
    serviceTypeName: application?.serviceType,
    limit: 10000,
    filter: '',
    xGroupUnit: undefined,
    yGroupUnit: undefined,
    backwardDirection: true,
    timestamp: undefined,
  });
  // 조회 파라미터가 effect로 한 박자 늦게 따라오므로 serviceName도 같은 effect에서 함께 갱신하고,
  // 파라미터와 같이 deferred로 넘긴다. 그러지 않으면 대상이 바뀌는 렌더에서 (이전 application,
  // 새 service) 짝의 queryKey가 한 번 만들어져, 그 service에 없는 application을 조회하게 된다.
  const [requestService, setRequestService] = React.useState(serviceName);
  const queryParams = React.useDeferredValue(query);
  const requestServiceName = React.useDeferredValue(requestService);
  const queryString = getQueryString(queryParams);

  React.useEffect(() => {
    setQueryParams((prev) => ({
      ...prev,
      from,
      to,
      application: application?.applicationName,
      serviceTypeName: application?.serviceType,
    }));
    setRequestService(serviceName);
  }, [application?.applicationName, application?.serviceType, from, to, serviceName]);

  const { data, isLoading } = useQuery({
    queryKey: [END_POINTS.SCATTER_DATA, queryString, requestServiceName],
    queryFn: queryFn(`${END_POINTS.SCATTER_DATA}${queryString}`, {
      serviceName: requestServiceName,
    }),
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
