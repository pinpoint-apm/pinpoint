import { GetApdexScore, END_POINTS, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString, toBasicISOString } from '@pinpoint-fe/ui/src/utils';
import { keepPreviousData, useQuery, useSuspenseQuery } from '@tanstack/react-query';
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
  /**
   * 조회할 기간. 훅이 URL을 직접 읽지 않고 호출자에게 받는다 — 조회 훅을 라우터 상태와
   * 분리해 두기 위한 것이다(`.claude/rules/api-hooks.md`). 읽는 자리는 `ApdexScoreFetcher`다.
   */
  dateRange: { from: Date; to: Date };
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
  dateRange,
  shouldPoll,
  agentId,
  serviceName,
}: UseGetApdexScoreProps) => {
  const queryString = getQueryString({
    from: toBasicISOString(dateRange.from),
    to: toBasicISOString(dateRange.to),
    applicationName: nodeData?.applicationName,
    serviceTypeName: nodeData?.serviceType,
    agentId,
  });

  const query = shouldPoll ? useQuery : useSuspenseQuery;
  const { data, isLoading } = query({
    queryKey: [END_POINTS.APDEX_SCORE, queryString, serviceName],
    queryFn: queryFn(`${END_POINTS.APDEX_SCORE}${queryString}`, { serviceName }),
    gcTime: shouldPoll ? 0 : 30000,
    staleTime: shouldPoll ? 0 : 30000,
    placeholderData: shouldPoll ? keepPreviousData : undefined,
  });

  return { data, isLoading };
};
