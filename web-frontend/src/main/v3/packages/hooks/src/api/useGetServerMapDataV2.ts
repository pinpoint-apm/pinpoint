import React from 'react';
import { GetServerMap, END_POINTS } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useServerMapSearchParameters } from '../searchParameters';
import { keepPreviousData, useQuery, useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<GetServerMap.Parameters>) => {
  if (
    queryParams.useStatisticsAgentState !== null &&
    queryParams.useStatisticsAgentState !== undefined &&
    queryParams.applicationName &&
    queryParams.serviceTypeName &&
    queryParams.from &&
    queryParams.to
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetServerMapDataV2 = ({
  shouldPoll = false,
  useStatisticsAgentState,
}: {
  shouldPoll: boolean;
  useStatisticsAgentState?: boolean;
}) => {
  const { dateRange, search, application, queryOption } = useServerMapSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();

  const [queryParams, setQueryParams] = React.useState<Partial<GetServerMap.Parameters>>({
    from,
    to,
    calleeRange: queryOption.inbound,
    callerRange: queryOption.outbound,
    wasOnly: !!queryOption.wasOnly,
    bidirectional: !!queryOption.bidirectional,
    useStatisticsAgentState,
    serviceTypeName: application?.serviceType,
    applicationName: application?.applicationName,
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
  ]);

  const query = shouldPoll ? useQuery : useSuspenseQuery;
  const { data, isLoading } = query({
    queryKey: [END_POINTS.SERVER_MAP_DATA_V2, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.SERVER_MAP_DATA_V2}${queryString}`) : () => null,
    gcTime: shouldPoll ? 0 : 30000,
    placeholderData: shouldPoll ? keepPreviousData : undefined,
    enabled: !!queryString,
  });

  return { data, isLoading };
};
