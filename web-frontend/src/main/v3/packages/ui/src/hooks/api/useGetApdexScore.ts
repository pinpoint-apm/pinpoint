import React from 'react';
import { GetApdexScore, END_POINTS, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { keepPreviousData, useQuery, useSuspenseQuery } from '@tanstack/react-query';
import { useServerMapSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<GetApdexScore.Parameters>) => {
  if (
    queryParams.from &&
    queryParams.to &&
    queryParams.applicationName &&
    queryParams.serviceTypeCode
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
};

export const useGetApdexScore = ({ nodeData, shouldPoll, agentId }: UseGetApdexScoreProps) => {
  const { dateRange } = useServerMapSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const [queryParams, setQueryParams] = React.useState<Partial<GetApdexScore.Parameters>>({
    from,
    to,
    applicationName: nodeData?.applicationName,
    serviceTypeCode: nodeData?.serviceTypeCode,
    agentId: agentId,
  });

  React.useEffect(() => {
    if (nodeData) {
      setQueryParams((prev) => ({
        ...prev,
        applicationName: nodeData?.applicationName,
        serviceTypeCode: nodeData?.serviceTypeCode,
        from: from,
        to: to,
        agentId,
      }));
    }
  }, [nodeData, from, to, agentId]);
  const queryString = getQueryString(queryParams);

  const query = shouldPoll ? useQuery : useSuspenseQuery;
  const { data, isLoading } = query({
    queryKey: [END_POINTS.APDEX_SCORE, queryString],
    queryFn: queryFn(`${END_POINTS.APDEX_SCORE}${queryString}`),
    gcTime: shouldPoll ? 0 : 30000,
    staleTime: shouldPoll ? 0 : 30000,
    placeholderData: shouldPoll ? keepPreviousData : undefined,
  });

  return { data, isLoading };
};
