import { END_POINTS, GetHistogramStatistics, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString, toBasicISOString } from '@pinpoint-fe/ui/src/utils';
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

// 링크는 `/servermap/linkHistogram`으로 조회한다. 양 끝 application을 map 응답의
// sourceInfo/targetInfo에서 그대로 가져온다(service까지 각각 실린다). serviceType은 desc와
// name이 어긋날 수 있어 쓰지 않고 serviceTypeCode를 쓴다. service는 파라미터로 실리므로
// service 헤더는 보내지 않는다.
const getLinkQueryString = (
  link: GetServerMap.LinkData | undefined,
  dateRange: { from: Date; to: Date },
) => {
  const source = link?.sourceInfo;
  const target = link?.targetInfo;
  if (
    !source?.serviceName ||
    !source?.applicationName ||
    source?.serviceTypeCode === undefined ||
    source?.serviceTypeCode === null ||
    !target?.serviceName ||
    !target?.applicationName ||
    target?.serviceTypeCode === undefined ||
    target?.serviceTypeCode === null
  ) {
    return '';
  }
  return (
    '?' +
    convertParamsToQueryString({
      from: toBasicISOString(dateRange.from),
      to: toBasicISOString(dateRange.to),
      fromServiceName: source.serviceName,
      fromApplicationName: source.applicationName,
      fromServiceTypeCode: source.serviceTypeCode,
      toServiceName: target.serviceName,
      toApplicationName: target.applicationName,
      toServiceTypeCode: target.serviceTypeCode,
    })
  );
};

export const useGetHistogramStatistics = ({
  applicationName,
  serviceType,
  dateRange,
  queryOption,
  useStatisticsAgentState,
  nodeKey,
  link,
  serviceName,
}: {
  applicationName?: string;
  serviceType?: string;
  dateRange: { from: Date; to: Date };
  queryOption: {
    inbound?: number;
    outbound?: number;
    wasOnly?: boolean;
    bidirectional?: boolean;
  };
  useStatisticsAgentState?: boolean;
  nodeKey?: string;
  link?: GetServerMap.LinkData;
  serviceName?: string;
}) => {
  const isLink = !!link;
  const queryString = isLink
    ? getLinkQueryString(link, dateRange)
    : getQueryString({
        from: toBasicISOString(dateRange.from),
        to: toBasicISOString(dateRange.to),
        calleeRange: queryOption.inbound,
        callerRange: queryOption.outbound,
        wasOnly: !!queryOption.wasOnly,
        bidirectional: !!queryOption.bidirectional,
        serviceTypeName: serviceType,
        applicationName,
        useStatisticsAgentState,
        nodeKey,
      });

  const { data, isLoading } = useQuery<GetHistogramStatistics.Response>({
    queryKey: isLink
      ? [END_POINTS.LINK_HISTOGRAM, queryString]
      : [END_POINTS.HISTOGRAM_STATISTICS, queryString, serviceName],
    queryFn: isLink
      ? queryFn(`${END_POINTS.LINK_HISTOGRAM}${queryString}`)
      : queryFn(`${END_POINTS.HISTOGRAM_STATISTICS}${queryString}`, { serviceName }),
    enabled: !!queryString,
  });

  return { data, isLoading };
};
