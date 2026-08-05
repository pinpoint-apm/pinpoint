import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { queryClient } from './reactQueryHelper';

/**
 * configuration은 웹이 뜬 뒤 바뀌지 않는 서버 설정이므로 캐시 엔트리를 하나만 두고 공유한다.
 * (`serviceScopedQueryKeyHashFn`의 service 무관 엔드포인트라 service를 바꿔도 같은 키다.)
 */
const CONFIGURATION_QUERY_KEY = [END_POINTS.CONFIGURATION];

const fetchConfiguration = async <T>(): Promise<T> => {
  const response = await fetch(`${END_POINTS.CONFIGURATION}`);

  if (!response?.ok) {
    const errorData = await response.json();
    throw new Error(errorData?.message);
  }

  return await response.json();
};

export const useGetConfiguration = <T>() => {
  const { data, error, isLoading, refetch } = useQuery<T>({
    queryKey: CONFIGURATION_QUERY_KEY,
    queryFn: fetchConfiguration,
    // 한 번 받은 값을 계속 쓴다. 페이지를 옮기며 이 훅이 remount 되어도 재요청하지 않는다.
    // (실패한 쿼리는 data가 없으므로 다음 mount에서 정상적으로 다시 시도된다.)
    staleTime: Infinity,
    gcTime: Infinity,
  });
  return { data, error, isLoading, refetch };
};

/**
 * 렌더 밖(라우트 로더 등)에서 configuration을 읽는 경로.
 *
 * 라우트 로더는 페이지에 진입할 때마다, 그리고 날짜 파라미터를 정규화하며 redirect 할 때마다
 * 다시 실행된다. 그래서 이 함수가 매번 fetch를 하면 페이지 이동마다 `/api/configuration`이
 * 재요청된다. `useGetConfiguration`과 같은 queryKey로 공유 캐시에 넣어 두어, 웹 접근 후
 * 최초 1회만 실제 요청이 나가고 이후에는 캐시된 값을 그대로 반환한다.
 *
 * 백엔드가 죽었을 때 로더가 기본값으로 진행하는 기존 동작을 유지하려면 재시도 없이 즉시
 * 실패해야 하므로 `retry: false`를 준다. 같은 이유로 `ignoreGlobalError`를 켜서 로더 단계의
 * 실패는 조용히 넘긴다. 이 실패는 로더가 catch 해 기본값으로 진행하는, 사용자에게 알릴 필요가
 * 없는 경로다. 사용자에게 보이는 에러 토스트는 (변경 전과 동일하게) 화면에서 실제로
 * configuration을 필요로 하는 `useGetConfiguration` 쪽 실패에서만 발생한다.
 * (`meta`는 fetch 단위 옵션이라 훅의 재요청에는 이 설정이 묻지 않는다.)
 */
export const getConfiguration = <T>(): Promise<T> =>
  queryClient.ensureQueryData<T>({
    queryKey: CONFIGURATION_QUERY_KEY,
    queryFn: fetchConfiguration,
    staleTime: Infinity,
    retry: false,
    meta: { ignoreGlobalError: true },
  });
