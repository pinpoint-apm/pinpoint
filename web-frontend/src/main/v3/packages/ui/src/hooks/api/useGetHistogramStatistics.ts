import { END_POINTS, GetHistogramStatistics } from '@pinpoint-fe/ui/src/constants';
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

export const useGetHistogramStatistics = ({
  applicationName,
  serviceType,
  dateRange,
  queryOption,
  useStatisticsAgentState,
  nodeKey,
  linkKey,
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
  linkKey?: string;
  serviceName?: string;
}) => {
  const queryString = getQueryString({
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
    linkKey,
  });

  const { data, isLoading } = useQuery<GetHistogramStatistics.Response>({
    queryKey: [END_POINTS.HISTOGRAM_STATISTICS, queryString, serviceName],
    queryFn: queryFn(
      linkKey
        ? `${END_POINTS.HISTOGRAM_STATISTICS_LINKS}${queryString}`
        : `${END_POINTS.HISTOGRAM_STATISTICS}${queryString}`,
      { serviceName },
    ),
    enabled: !!queryString,
  });

  return { data, isLoading };
};
