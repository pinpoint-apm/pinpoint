import { useQuery } from '@tanstack/react-query';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import { END_POINTS, GetServices } from '@pinpoint-fe/ui/src/constants';
import { queryClient, queryFn } from './reactQueryHelper';

const SERVICES_QUERY_KEY = [END_POINTS.SERVICES];

const withDefaultService = (data: GetServices.Response) =>
  data.includes(DEFAULT_SERVICE) ? data : [DEFAULT_SERVICE, ...data];

export const useGetServices = ({ enabled = true }: { enabled?: boolean } = {}) => {
  return useQuery<GetServices.Response>({
    queryKey: SERVICES_QUERY_KEY,
    queryFn: queryFn(END_POINTS.SERVICES),
    enabled,
    select: withDefaultService,
  });
};

/**
 * 렌더 밖(라우트 로더)에서 service 목록을 읽는다. 훅과 같은 캐시를 쓰므로 화면이 곧이어
 * `useGetServices`로 읽을 때 요청이 한 번 더 나가지 않는다. (이 key는 service 단위로 갈리지 않는다 —
 * `SERVICE_AGNOSTIC_ENDPOINTS`)
 */
export const getServices = async (): Promise<GetServices.Response> =>
  withDefaultService(
    await queryClient.ensureQueryData<GetServices.Response>({
      queryKey: SERVICES_QUERY_KEY,
      queryFn: queryFn(END_POINTS.SERVICES),
      retry: false,
      // 실패는 로더가 스스로 처리한다(경로를 그대로 둔다). 화면의 훅이 같은 요청으로 다시 알린다.
      meta: { ignoreGlobalError: true },
    }),
  );
