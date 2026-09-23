import { END_POINTS, ErrorResponse } from '@pinpoint-fe/ui/src/constants';
import {
  hashKey,
  MutationCache,
  QueryCache,
  QueryClient,
  type Query,
  type QueryCacheNotifyEvent,
  type QueryKey,
} from '@tanstack/react-query';
import { toast } from 'react-toastify';
import { getDefaultStore } from 'jotai';
import {
  forbiddenNoticeMountCountAtom,
  forbiddenTargetGenerationAtom,
  markCurrentPathForbiddenAtom,
  toastCountAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { coversPageOnForbidden, getCurrentRouterPath } from '@pinpoint-fe/ui/src/utils';
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

/** 권한 없음. 서버는 ProblemDetail(title: "Forbidden")로 내려준다. */
export const HTTP_STATUS_FORBIDDEN = 403;

function isServerErrorResponse(body: unknown): body is ErrorResponse {
  const o = body as Record<string, unknown>;
  return (
    o != null &&
    typeof o === 'object' &&
    typeof o.status === 'number' &&
    (typeof o.detail === 'string' || typeof o.title === 'string')
  );
}

/**
 * 응답 상태 코드를 실은 Error. **모든 실패 경로가 이 함수를 지나야 한다.**
 *
 * 권한 판정(403)이 이 값으로 갈리는데, 상태 코드를 body에서만 읽으면 조용히 사라진다 —
 * ProblemDetail이 아닌 body(프록시·게이트웨이가 끼워 넣은 HTML/평문)거나 JSON 파싱 자체가
 * 실패하면 `status`가 없는 Error가 던져져, 403이 평범한 조회 실패와 구분되지 않는다.
 */
const responseError = (response: Response, message: string, serverError?: ErrorResponse) => {
  const err = new Error(message) as Error & ErrorResponse;

  if (serverError) {
    Object.assign(err, serverError);
    err.message = message;
  }
  // body가 준 status보다 응답의 것이 기준이다. 둘은 ProblemDetail에서 같은 값이고,
  // 어긋난다면 그것은 서버 쪽 사정이라 전송 계층의 값을 믿는 편이 안전하다.
  err.status = response.status;

  return err;
};

export async function parseResponseError(response: Response): Promise<never> {
  let body: unknown;
  try {
    body = await response.json();
  } catch {
    throw responseError(
      response,
      `Request failed with status ${response.status}. An error occurred while fetching the data.`,
    );
  }

  if (isServerErrorResponse(body)) {
    const serverError = body;
    throw responseError(
      response,
      serverError.detail || serverError.title || 'An error occurred while fetching the data.',
      serverError,
    );
  }

  const detail =
    typeof (body as Record<string, unknown>)?.detail === 'string'
      ? (body as Record<string, unknown>).detail
      : typeof (body as Record<string, unknown>)?.message === 'string'
        ? (body as Record<string, unknown>).message
        : undefined;
  throw responseError(response, (detail as string) || 'An error occurred while fetching the data.');
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
    // v11 에서 bodyClassName 옵션이 없어졌다. 본문 정렬 규칙은 globals.css 의
    // `.pp-toast-body-top` 로 옮겼다.
    className: 'pointer-events-auto pp-toast-body-top',
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
  if (isForbidden(error)) {
    // 이 조회가 **지금 화면의 것인지**. 응답은 늦게 올 수 있어서, 요청을 낸 화면을 떠난 뒤에
    // 도착하기도 한다(공유 `queryFn`은 abort signal을 쓰지 않아 요청이 계속된다).
    // 둘 중 하나면 지금 화면의 것이다:
    // - 옵저버가 있다. 옵저버는 마운트된 컴포넌트에만 붙고 그 컴포넌트는 지금 화면의 것이다.
    // - 요청을 시작한 화면·대상이 지금 그대로다(`isSentFromCurrentTarget`).
    //   **suspense 조회의 첫 요청은 옵저버가 0이다** — suspend된 컴포넌트는 commit되지 않아
    //   구독하지 않는다. 옵저버 수만 보면 떠난 화면의 답으로 판정해 지우고, React가 다시
    //   렌더하며 새 쿼리를 만들어 또 묻는다. 그것이 또 403이 되어, 화면은 덮이지 않은 채
    //   요청이 끝없이 반복됐다.
    const isActive = query.getObserversCount() > 0 || isSentFromCurrentTarget(query);
    // 덮을 화면이면서, 그 화면이 실제로 안내를 그리는지. 안내를 그리지 않는 화면(설정의 Alarm,
    // Users 등)을 덮는다고 판정하면 본문이 언마운트되지 않아, 아래에서 지운 조회를 곧바로 다시 묻는다.
    const coversPage =
      coversPageOnForbidden(getCurrentRouterPath()) &&
      getDefaultStore().get(forbiddenNoticeMountCountAtom) > 0;

    // 지금 화면이 쓰는 조회가 막혔을 때만 그 화면을 덮는다. 떠난 화면의 답으로 덮으면,
    // 권한이 멀쩡한 새 화면이 권한 없음으로 보인다.
    if (isActive && coversPage) {
      getDefaultStore().set(markCurrentPathForbiddenAtom);
    }
    if (!isActive || coversPage) {
      forgetForbiddenQuery(query);
    }
    // 403은 토스트를 띄우지 않는다. 화면이 권한 없음으로 바뀌는 곳에서는 같은 사실을 두 번
    // 알리는 것이고, 바뀌지 않는 곳(map)에서는 위젯의 인라인 안내가 이미 어디가 막혔는지를
    // 가리킨다. 게다가 폴링하는 화면에서는 tick마다 같은 토스트가 쌓인다.
    return;
  }
  if (query.meta?.ignoreGlobalError) return;
  showGlobalErrorToast(error, { toastId: query.queryHash });
};

interface FetchStart {
  path: string;
  targetGeneration: number;
}

/**
 * 쿼리마다 **요청을 시작한 경로와 대상 세대**. (이슈 #10744)
 *
 * 403 응답이 지금 화면의 것인지 가를 때 쓴다(`handleGlobalQueryError`). 옵저버 수로는
 * suspense 조회의 첫 요청을 가를 수 없다 — 그 요청은 옵저버 없이 나간다.
 * 경로는 도장(`markCurrentPathForbiddenAtom`)과 같은 출처(`getCurrentRouterPath`)로 읽는다.
 * 경로만으로는 화면 안에서 대상을 바꾼 것을 알 수 없어 세대를 함께 적는다
 * (`forbiddenTargetGenerationAtom`).
 */
const fetchStarts = new WeakMap<Query<unknown, unknown, unknown, readonly unknown[]>, FetchStart>();

const getFetchStart = (): FetchStart => ({
  path: getCurrentRouterPath(),
  targetGeneration: getDefaultStore().get(forbiddenTargetGenerationAtom),
});

/** 이 쿼리의 요청이 지금 보고 있는 화면·대상에서 시작되었는지. */
const isSentFromCurrentTarget = (query: Query<unknown, unknown, unknown, readonly unknown[]>) => {
  const sent = fetchStarts.get(query);
  const current = getFetchStart();

  return (
    sent !== undefined &&
    sent.path === current.path &&
    sent.targetGeneration === current.targetGeneration
  );
};

/**
 * 요청이 시작될 때 그 경로와 대상 세대를 적는다. `fetch` 액션은 요청을 시작할 때 한 번 dispatch되고
 * 재시도에서는 다시 오지 않으므로, 재시도 끝에 온 응답도 처음 요청한 때의 값으로 판정된다.
 * 캐시 이벤트는 dispatch 안에서 동기로 흐르므로 응답보다 늦게 적힐 일이 없다.
 */
const recordFetchStart = (event: QueryCacheNotifyEvent) => {
  if (event.type === 'updated' && event.action.type === 'fetch') {
    fetchStarts.set(event.query, getFetchStart());
  }
};

/**
 * 403을 받은 쿼리를 캐시에서 **지운다.** (이슈 #10744)
 *
 * 여러 화면이 같은 조회를 공유한다 — 사이드바의 agent 목록(`/api/agents`)은 Inspector·URL
 * Statistic·Error Analysis·OpenTelemetry가 같은 queryKey로 부른다. 에러가 캐시에 남으면 그
 * 중 한 화면에서 403을 받은 뒤 다른 화면으로 옮겼을 때, react-query가 **재조회 없이 캐시에
 * 담긴 에러를 렌더 중에 그대로 던진다**(`useBaseQuery`의 `getHasError` → `throw result.error`).
 * 그 경로에서는 요청이 나가지 않으므로 이 핸들러도 불리지 않아, 두 번째 화면은 403을 받고도
 * 권한 없음으로 바뀌지 않았다.
 *
 * 지워 두면 다음 화면이 실제로 다시 묻는다. 막혀 있으면 403이 다시 와서 그 화면도 덮이고,
 * 권한이 생겼으면 그대로 그려진다 — 권한 상태를 캐시로 기억할 이유가 없다.
 *
 * **화면을 덮거나, 이 조회가 떠난 화면의 것일 때만 지운다.** 덮이는 화면은 본문이 곧
 * 언마운트되므로(도장을 찍는 것이 옵저버 알림보다 먼저다 — `Query#onError`가 `#dispatch` 뒤
 * 동기로 불리고, 알림은 notifyManager가 모아서 흘린다) 그 자리에서 다시 묻지 않는다. 덮지 않는
 * 화면(map)에서 보고 있는 조회를 지우면 에러를 그린 채로 남은 컴포넌트가 다음 렌더에 **새 쿼리를
 * 만들어 곧바로 다시 요청하고**, 그것이 또 403이 되어 요청이 끝없이 반복된다.
 * 안내를 그리지 않는 화면도 같다 — 경로로는 덮을 화면이어도 본문이 남으므로 map과 똑같이 둔다
 * (`forbiddenNoticeMountCountAtom`).
 */
const forgetForbiddenQuery = (query: Query<unknown, unknown, unknown, readonly unknown[]>) => {
  queryCache.remove(query);
};

/**
 * 권한 없음 응답인지. (이슈 #10744)
 *
 * - 판정은 상태 코드로만 한다. `detail`("Access denied")은 서버가 문구를 바꾸면 그대로 어긋나고,
 *   `title`("Forbidden")도 ProblemDetail이 아닌 응답에는 없다.
 *   → `CustomExceptionHandler#handleAccessDeniedException`
 * - 대상은 쿼리(GET)뿐이다. 뮤테이션은 `MutationCache`로 가므로 여기까지 오지 않는다 —
 *   저장이 막힌 것과 화면을 볼 수 없는 것은 다른 일이고, 전자는 토스트로 알린다.
 *
 * 어떤 엔드포인트였는지는 따지지 않는다. 화면이 부르는 API 중 하나라도 막혔으면 그 화면은
 * 온전하지 않다. 어느 화면이 그 판정에서 빠지는지는 `coversPageOnForbidden`이 정한다.
 *
 * 경로는 **에러가 온 시점의 경로**로 판정한다. 화면을 떠난 뒤 도착한 응답이라도 도장은 지금
 * 경로에 찍히므로, 남의 화면을 덮지는 않는다.
 */
const isForbidden = (error: unknown) =>
  (error as ErrorResponse | undefined)?.status === HTTP_STATUS_FORBIDDEN;

/** react-query의 기본 재시도 횟수. 403이 아닌 실패는 예전과 똑같이 재시도한다. */
const DEFAULT_QUERY_RETRY_COUNT = 3;

/**
 * 403은 다시 묻지 않는다. (이슈 #10744)
 *
 * 전역 에러 핸들러(`handleGlobalQueryError`)는 재시도를 모두 마친 뒤에야 불린다. 기본값대로
 * 재시도하면 권한 없음 안내가 뜨기까지 막힌 요청이 세 번 더 나가고 약 7초(1s·2s·4s 백오프)를
 * 기다린다. 권한은 몇 초 사이에 바뀌지 않으므로 다시 물어도 답이 같다.
 *
 * 훅이 `retry`를 직접 준 경우(`retry: false` 등)는 그 값이 이긴다.
 */
export const retryUnlessForbidden = (failureCount: number, error: unknown) =>
  !isForbidden(error) && failureCount < DEFAULT_QUERY_RETRY_COUNT;

const mutationCache = new MutationCache({
  onError: (error, _variables, _context, mutation) => {
    if (mutation.meta?.ignoreGlobalError) return;
    showGlobalErrorToast(error);
  },
});

const queryCache = new QueryCache({
  onError: handleGlobalQueryError,
});
queryCache.subscribe(recordFetchStart);

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
 * `enableServiceMap`이 꺼져 있으면 그 함수가 undefined를 주고, 헤더도 실리지 않아 모든 요청이
 * 기본 service의 조회다. 그때는 나눌 기준이 없으므로 덧붙이지 않는다 — 덧붙이면 헤더가 같은
 * 요청들이 전역 선택값에 따라 다른 키에 쌓여 같은 데이터를 다시 받는다.
 *
 * queryKey 배열 자체는 그대로 두므로 `invalidateQueries`/`removeQueries`의 부분 매칭
 * (queryKey 구조 비교)은 영향받지 않는다.
 */
export const serviceScopedQueryKeyHashFn = (queryKey: QueryKey) => {
  const requestService = SERVICE_AGNOSTIC_ENDPOINTS.includes(String(queryKey[0]))
    ? undefined
    : getRequestService();

  return requestService ? hashKey([...queryKey, requestService]) : hashKey(queryKey);
};

export const queryClient = new QueryClient({
  queryCache,
  mutationCache,
  defaultOptions: {
    queries: {
      retry: retryUnlessForbidden,
      refetchOnWindowFocus: false,
      staleTime: 3000,
      queryKeyHashFn: serviceScopedQueryKeyHashFn,
    },
  },
});
