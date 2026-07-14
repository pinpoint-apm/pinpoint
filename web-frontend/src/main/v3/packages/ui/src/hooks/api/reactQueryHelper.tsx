import { ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import { MutationCache, QueryCache, QueryClient, type Query } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { getDefaultStore } from 'jotai';
import { toastCountAtom } from '@pinpoint-fe/ui/src/atoms';
import { ErrorToast } from '../../components/Error/ErrorToast';

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

export const queryFn = (url: string) => async () => {
  const response = await fetch(url);

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

export const queryClient = new QueryClient({
  queryCache,
  mutationCache,
  defaultOptions: { queries: { refetchOnWindowFocus: false, staleTime: 3000 } },
});
