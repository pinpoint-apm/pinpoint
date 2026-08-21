import { END_POINTS, GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

/**
 * @param serviceName 이 조회가 나갈 service. 지정하면 `pServiceName` 헤더를 직접 싣고 캐시도 그
 * 단위로 갈린다. 화면의 service와 조회 대상의 service가 다를 때 넘긴다 — servicemap은 다른
 * service의 application도 함께 그리기 때문이다(`useServerMapTargetServiceName`). 넘기지 않으면
 * 기존대로 fetch 인터셉터가 경로/전역 선택값으로 결정한다.
 */
export const useGetHeatmapAppData = (
  parameters: GetHeatmapAppData.Parameters,
  serviceName?: string,
) => {
  const queryString = `?${convertParamsToQueryString(parameters)}`;
  const { data, isLoading, refetch, error } = useQuery<GetHeatmapAppData.Response>({
    queryKey: [END_POINTS.HEATMAP_APP_DATA, parameters, serviceName],
    queryFn: queryFn(`${END_POINTS.HEATMAP_APP_DATA}${queryString}`, { serviceName }),
    enabled: !!queryString,
    // HeatmapFetcher renders a full inline error overlay for this failure — suppress the
    // redundant global error toast so the user sees a single error signal.
    meta: { ignoreGlobalError: true },
  });
  return { data, isLoading, refetch, error };
};
