import { END_POINTS, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import {
  hashKey,
  MutationCache,
  QueryCache,
  QueryClient,
  type Query,
  type QueryKey,
} from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { getDefaultStore } from 'jotai';
import { toastCountAtom } from '@pinpoint-fe/ui/src/atoms';
import { ErrorToast } from '../../components/Error/ErrorToast';
import { getRequestService, SERVICE_NAME_HEADER } from './serviceNameFetchInterceptor';

declare module '@tanstack/react-query' {
  interface Register {
    mutationMeta: {
      /** true로 설정하면 MutationCache의 글로벌 에러 토스트를 표시하지 않음. 컴포넌트에서 에러를 자체 처리할 때 사용. */
      ignoreGlobalError?: boolean;
    };
    queryMeta: {
      /** true로 설정하면 QueryCache의 글로벌 에러 토스트를 표시하지 않음. 컴포넌트에서 에러를 자체 처리할 때 사용. */
      ignoreGlobalError?: boolean;
    };
  }
}

function isServerErrorResponse(body: unknown): body is ErrorResponse {
  const o = body as Record<string, unknown>;
  return (
    o != null &&
    typeof o === 'object' &&
    typeof o.status === 'number' &&
    (typeof o.detail === 'string' || typeof o.title === 'string')
  );
}

export async function parseResponseError(response: Response): Promise<never> {
  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw new Error(
      `Request failed with status ${response.status}. An error occurred while fetching the data.`,
    );
  }

  if (isServerErrorResponse(body)) {
    const serverError = body;
    const err = new Error(
      serverError.detail || serverError.title || 'An error occurred while fetching the data.',
    ) as Error & ErrorResponse;
    Object.assign(err, serverError);
    err.message = serverError.detail || serverError.title || err.message;
    throw err;
  }

  const detail =
    typeof (body as Record<string, unknown>)?.detail === 'string'
      ? (body as Record<string, unknown>).detail
      : typeof (body as Record<string, unknown>)?.message === 'string'
        ? (body as Record<string, unknown>).message
        : undefined;
  throw new Error((detail as string) || 'An error occurred while fetching the data.');
}

export interface QueryFnOptions {
  /**
   * 이 요청이 조회할 service. 지정하면 `pServiceName` 헤더를 여기서 직접 싣고,
   * fetch 인터셉터는 이미 실린 헤더를 덮어쓰지 않는다.
   *
   * 화면의 service와 조회 대상의 service가 다를 때 쓴다(`useServerMapTargetServiceName`).
   * 인터셉터는 경로/전역 선택값만 보므로 "고른 노드가 다른 service 소속"이라는 사실을 알 수 없다.
   *
   * 이 값을 싣는 훅은 queryKey에도 같은 값을 넣어야 한다. 헤더만 갈리고 캐시 키가 같으면
   * 이름이 같은 다른 service의 application끼리 캐시가 섞인다.
   */
  serviceName?: string;
}

export const queryFn = (url: string, options?: QueryFnOptions) => async () => {
  const response = options?.serviceName
    ? await fetch(url, { headers: { [SERVICE_NAME_HEADER]: options.serviceName } })
    : await fetch(url);

  if (!response.ok) {
    await parseResponseError(response);
  }
  return response.json();
};

/**
 * 글로벌 에러 토스트. 모듈 레벨이라 useReactToastifyToast 훅을 쓸 수 없으므로, 동일한
 * default store를 통해 toastCountAtom을 직접 갱신해 "Clear All" 동작을 토스트와 일치시킨다.
 * toastId를 지정하면 react-toastify가 동일 id의 중복 토스트를 억제한다(폴링 실패 누적 방지).
 */
export const showGlobalErrorToast = (error: unknown, options?: { toastId?: string }) => {
  const store = getDefaultStore();
  toast.error(<ErrorToast error={error as Error} />, {
    toastId: options?.toastId,
    className: 'pointer-events-auto',
    bodyClassName: '!items-start',
    autoClose: false,
    onOpen: () => store.set(toastCountAtom, (prev) => prev + 1),
    onClose: () => store.set(toastCountAtom, (prev) => (prev === 0 ? prev : prev - 1)),
  });
};

/**
 * 모든 쿼리(GET) 실패에 대한 글로벌 에러 토스트. 뮤테이션과 대칭 동작이며, React Query는
 * 기본적으로 쿼리 에러를 throw하지 않으므로 이 핸들러가 없으면 실패가 조용히 삼켜진다.
 * 컴포넌트가 에러를 자체 처리하는 경우 meta.ignoreGlobalError로 opt-out 할 수 있다.
 * ErrorBoundary는 인라인 fallback UI만 담당하므로 토스트는 여기서만 발생한다(중복 방지).
 */
export const handleGlobalQueryError = (
  error: unknown,
  query: Query<unknown, unknown, unknown, readonly unknown[]>,
) => {
  if (query.meta?.ignoreGlobalError) return;
  showGlobalErrorToast(error, { toastId: query.queryHash });
};

const mutationCache = new MutationCache({
  onError: (error, _variables, _context, mutation) => {
    if (mutation.meta?.ignoreGlobalError) return;
    showGlobalErrorToast(error);
  },
});

const queryCache = new QueryCache({
  onError: handleGlobalQueryError,
});

/**
 * service와 무관하게 항상 같은 응답을 주는 엔드포인트. 이들까지 service별로 분리하면
 * service를 바꿀 때마다 불필요한 재요청이 생기고, configuration은 특히
 * `InitialFetchOutlet`이 로드되기 전까지 자식을 렌더하지 않으므로 화면이 잠깐 사라진다.
 * queryKey의 첫 요소가 엔드포인트라는 이 저장소의 관례를 이용해 한 곳에서 예외를 관리한다.
 */
const SERVICE_AGNOSTIC_ENDPOINTS: string[] = [
  END_POINTS.CONFIGURATION,
  END_POINTS.SERVICES,
  END_POINTS.SERVER_TIME,
];

/**
 * 위 예외를 제외한 모든 쿼리 캐시를 "그 요청이 해석되는 service" 단위로 분리한다.
 *
 * pServiceName 헤더는 fetch 인터셉터가 붙이기 때문에 queryKey에 드러나지 않는다. 그래서
 * service가 다른 두 요청이 같은 캐시 엔트리를 공유했고, staleTime(3초) 안에 service를 바꾸면
 * 재요청 없이 이전 service의 데이터가 그대로 표시됐다.
 * 헤더와 동일한 규칙(`getRequestService`)으로 파생한 service를 해시에만 덧붙여 이를 막는다.
 *
 * queryKey 배열 자체는 그대로 두므로 `invalidateQueries`/`removeQueries`의 부분 매칭
 * (queryKey 구조 비교)은 영향받지 않는다.
 */
export const serviceScopedQueryKeyHashFn = (queryKey: QueryKey) =>
  SERVICE_AGNOSTIC_ENDPOINTS.includes(String(queryKey[0]))
    ? hashKey(queryKey)
    : hashKey([...queryKey, getRequestService()]);

export const queryClient = new QueryClient({
  queryCache,
  mutationCache,
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      staleTime: 3000,
      queryKeyHashFn: serviceScopedQueryKeyHashFn,
    },
  },
});
