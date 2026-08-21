import React from 'react';
import { GetApdexScore, END_POINTS, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString, toBasicISOString } from '@pinpoint-fe/ui/src/utils';
import { keepPreviousData, useQuery, useSuspenseQuery } from '@tanstack/react-query';
import { useServerMapSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<GetApdexScore.Parameters>) => {
  if (
    queryParams.from &&
    queryParams.to &&
    queryParams.applicationName &&
    (queryParams.serviceTypeName || queryParams.serviceTypeCode)
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export type UseGetApdexScoreProps = {
  nodeData?: GetServerMap.NodeData;
  disableFetch?: boolean;
  shouldPoll?: boolean;
  agentId?: string;
  /**
   * 이 조회가 나갈 service. 지정하면 `pServiceName` 헤더를 직접 싣고 캐시도 그 단위로 갈린다.
   *
   * 화면의 service와 조회 대상의 service가 다를 때 넘긴다 — servicemap은 다른 service의
   * application도 함께 그리기 때문이다(`useServerMapTargetServiceName`). 넘기지 않으면 기존대로
   * fetch 인터셉터가 경로/전역 선택값으로 결정한다.
   */
  serviceName?: string;
};

export const useGetApdexScore = ({
  nodeData,
  shouldPoll,
  agentId,
  serviceName,
}: UseGetApdexScoreProps) => {
  const { dateRange } = useServerMapSearchParameters();
  const from = toBasicISOString(dateRange.from);
  const to = toBasicISOString(dateRange.to);
  const [queryParams, setQueryParams] = React.useState<Partial<GetApdexScore.Parameters>>({
    from,
    to,
    applicationName: nodeData?.applicationName,
    serviceTypeName: nodeData?.serviceType,
    agentId: agentId,
  });
  // 조회 파라미터가 effect로 한 박자 늦게 따라오므로 serviceName도 같은 effect에서 함께 갱신한다.
  // 그러지 않으면 대상이 바뀌는 렌더에서 (이전 application, 새 service) 짝의 queryKey가 한 번
  // 만들어져, 그 service에 없는 application을 조회하는 요청이 나간다.
  const [requestServiceName, setRequestServiceName] = React.useState(serviceName);

  React.useEffect(() => {
    if (nodeData) {
      setQueryParams((prev) => ({
        ...prev,
        applicationName: nodeData?.applicationName,
        serviceTypeName: nodeData?.serviceType,
        from: from,
        to: to,
        agentId,
      }));
      setRequestServiceName(serviceName);
    }
  }, [nodeData, from, to, agentId, serviceName]);

  const queryString = getQueryString(queryParams);

  const query = shouldPoll ? useQuery : useSuspenseQuery;
  const { data, isLoading } = query({
    queryKey: [END_POINTS.APDEX_SCORE, queryString, requestServiceName],
    queryFn: queryFn(`${END_POINTS.APDEX_SCORE}${queryString}`, {
      serviceName: requestServiceName,
    }),
    gcTime: shouldPoll ? 0 : 30000,
    staleTime: shouldPoll ? 0 : 30000,
    placeholderData: shouldPoll ? keepPreviousData : undefined,
  });

  return { data, isLoading };
};
