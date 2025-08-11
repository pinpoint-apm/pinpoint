import React from 'react';
import { END_POINTS, GetHistogramStatistics } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
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
}: {
  useStatisticsAgentState?: boolean;
  nodeKey?: string;
  linkKey?: string;
}) => {
  const { dateRange, search, application, queryOption } = useServerMapSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();

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
