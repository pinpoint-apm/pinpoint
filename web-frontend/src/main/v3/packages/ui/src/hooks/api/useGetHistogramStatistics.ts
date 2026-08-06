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
}) => {
  const {
    dateRange,
    search,
    application: applicationFromPath,
    queryOption,
  } = useServerMapSearchParameters();
  const application = applicationFromPath ?? fallbackApplication;
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
  }, [
    application?.applicationName,
    application?.serviceType,
    from,
    to,
    search,
    useStatisticsAgentState,
    nodeKey,
    linkKey,
  ]);

  const { data, isLoading } = useQuery<GetHistogramStatistics.Response>({
    queryKey: [END_POINTS.HISTOGRAM_STATISTICS, queryString],
    queryFn: queryFn(
      linkKey
        ? `${END_POINTS.HISTOGRAM_STATISTICS_LINKS}${queryString}`
        : `${END_POINTS.HISTOGRAM_STATISTICS}${queryString}`,
    ),
    enabled: !!queryString,
  });

  return { data, isLoading };
};
