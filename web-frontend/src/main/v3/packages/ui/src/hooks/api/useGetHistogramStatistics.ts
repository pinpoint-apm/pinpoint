import React from 'react';
import { ApplicationType, END_POINTS, GetHistogramStatistics } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString, toBasicISOString } from '@pinpoint-fe/ui/src/utils';
import { useServerMapSearchParameters } from '../searchParameters';
import { useQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<GetHistogramStatistics.Parameters>) => {
  if (
    queryParams.useStatisticsAgentState !== null &&
    queryParams.useStatisticsAgentState !== undefined &&
    queryParams.applicationName &&
    queryParams.serviceTypeName &&
    queryParams.from &&
    queryParams.to &&
    queryParams.nodeKey // 원래 optional
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetHistogramStatistics = ({
  useStatisticsAgentState,
  nodeKey,
  linkKey,
  fallbackApplication,
  ignorePathApplication,
  serviceName,
}: {
  useStatisticsAgentState?: boolean;
  nodeKey?: string;
  linkKey?: string;
  /**
   * 경로에 application이 없을 때 기준 application으로 쓸 값.
   * service 전체를 모아 그린 servicemap에는 경로에 application이 없는데,
   * 통계 API는 기준 application이 필수라 선택된 노드/링크에서 끌어와야 한다.
   */
  fallbackApplication?: ApplicationType;
  /**
   * true면 경로의 application을 무시하고 `fallbackApplication`을 기준으로 쓴다.
   *
   * 다른 service의 노드를 고른 경우가 여기에 해당한다. 이 요청은 그 노드의 service로 나가는데
   * (`serviceName`) 경로의 application은 지금 화면의 service 소속이라, 백엔드가 그 이름을
   * 노드의 service에서 찾게 되어 기준이 성립하지 않는다.
   */
  ignorePathApplication?: boolean;
  /**
   * 이 조회가 나갈 service. 지정하면 `pServiceName` 헤더를 직접 싣고 캐시도 그 단위로 갈린다.
   *
   * 화면의 service와 조회 대상의 service가 다를 때 넘긴다 — servicemap은 다른 service의
   * application도 함께 그리기 때문이다(`useServerMapTargetServiceName`). 넘기지 않으면 기존대로
   * fetch 인터셉터가 경로/전역 선택값으로 결정한다.
   */
  serviceName?: string;
}) => {
  const {
    dateRange,
    search,
    application: applicationFromPath,
    queryOption,
  } = useServerMapSearchParameters();
  const application =
    (ignorePathApplication ? undefined : applicationFromPath) ?? fallbackApplication;
  const from = toBasicISOString(dateRange.from);
  const to = toBasicISOString(dateRange.to);

  const [queryParams, setQueryParams] = React.useState<Partial<GetHistogramStatistics.Parameters>>({
    from,
    to,
    calleeRange: queryOption.inbound,
    callerRange: queryOption.outbound,
    wasOnly: !!queryOption.wasOnly,
    bidirectional: !!queryOption.bidirectional,
    serviceTypeName: application?.serviceType,
    applicationName: application?.applicationName,
    useStatisticsAgentState,
    nodeKey,
    linkKey,
  });
  // 조회 파라미터가 effect로 한 박자 늦게 따라오므로 serviceName도 같은 effect에서 함께 갱신한다.
  // 그러지 않으면 대상이 바뀌는 렌더에서 (이전 application, 새 service) 짝의 queryKey가 한 번
  // 만들어져, 그 service에 없는 application을 조회하는 요청이 나간다.
  const [requestServiceName, setRequestServiceName] = React.useState(serviceName);
  const queryString = getQueryString(queryParams);

  React.useEffect(() => {
    setQueryParams((prev) => ({
      ...prev,
      applicationName: application?.applicationName,
      serviceTypeName: application?.serviceType,
      calleeRange: queryOption.inbound,
      callerRange: queryOption.outbound,
      wasOnly: !!queryOption.wasOnly,
      bidirectional: !!queryOption.bidirectional,
      useStatisticsAgentState,
      nodeKey,
      linkKey,
      from,
      to,
    }));
    setRequestServiceName(serviceName);
  }, [
    application?.applicationName,
    application?.serviceType,
    from,
    to,
    search,
    useStatisticsAgentState,
    nodeKey,
    linkKey,
    serviceName,
  ]);

  const { data, isLoading } = useQuery<GetHistogramStatistics.Response>({
    queryKey: [END_POINTS.HISTOGRAM_STATISTICS, queryString, requestServiceName],
    queryFn: queryFn(
      linkKey
        ? `${END_POINTS.HISTOGRAM_STATISTICS_LINKS}${queryString}`
        : `${END_POINTS.HISTOGRAM_STATISTICS}${queryString}`,
      { serviceName: requestServiceName },
    ),
    enabled: !!queryString,
  });

  return { data, isLoading };
};
