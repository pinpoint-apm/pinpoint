import type { Query } from '@tanstack/react-query';

// ErrorToast transitively pulls the ECharts (ESM) stack that babel-jest does not
// transform, so stub it out. These mocks must run before reactQueryHelper (and the
// mocked react-toastify) are imported, so those imports are placed after them.
jest.mock('../../components/Error/ErrorToast', () => ({ ErrorToast: () => null }));
jest.mock('react-toastify', () => ({ toast: { error: jest.fn() } }));
jest.mock('@pinpoint-fe/ui/src/atoms', () => {
  // serviceNameFetchInterceptor가 읽는 atom들도 함께 제공해야 한다(실제 atom이어야 store 읽기가 동작).
  const { atom } = require('jotai');
  // 권한 아톰은 판정 규칙(경로 도장)까지 검증 대상이므로 실제 모듈을 그대로 쓴다.
  const permission = jest.requireActual('@pinpoint-fe/ui/src/atoms/permission');
  return {
    ...permission,
    toastCountAtom: atom(0),
    selectedServiceAtom: atom('DEFAULT'),
    configurationAtom: atom(undefined),
    DEFAULT_SERVICE: 'DEFAULT',
  };
});

import { toast } from 'react-toastify';
import { getDefaultStore } from 'jotai';
import { QueryClient } from '@tanstack/react-query';
import {
  configurationAtom,
  forbiddenPathAtom,
  selectedServiceAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { Configuration, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { SERVICE_NAME_HEADER } from './serviceNameFetchInterceptor';
import {
  enterForbiddenNotice,
  handleGlobalQueryError,
  parseResponseError,
  queryClient,
  queryFn,
  retryUnlessForbidden,
  serviceScopedQueryKeyHashFn,
  showGlobalErrorToast,
} from './reactQueryHelper';

// 옵저버 수는 "지금 화면이 이 조회를 쓰고 있는가"를 가른다. 기본값은 쓰고 있는 상태다.
const makeQuery = ({ observers = 1, ...over }: Partial<Query> & { observers?: number } = {}) =>
  ({
    queryHash: '["/api/test",""]',
    queryKey: ['/api/test'],
    meta: undefined,
    getObserversCount: () => observers,
    ...over,
  }) as unknown as Query<unknown, unknown, unknown, readonly unknown[]>;

const forbidden = (status = 403) => Object.assign(new Error('Access denied'), { status });

/**
 * 실제 캐시에 올라간 쿼리를 만든다. `setQueryData`로 만든 쿼리는 옵저버가 0이므로, 화면이
 * 보고 있는 상태를 흉내 낼 때는 옵저버 수를 덮어쓴다(캐시에서 지우는 판단이 이 값으로 갈린다).
 */
const cachedQuery = (queryKey: unknown[], observers: number) => {
  queryClient.setQueryData(queryKey, []);
  const query = queryClient.getQueryCache().find({ queryKey });
  query!.getObserversCount = () => observers;
  return query as unknown as Query;
};

describe('reactQueryHelper global query error handling', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('shows a global error toast for a failed query', () => {
    handleGlobalQueryError(new Error('boom'), makeQuery({ queryHash: 'hash-1' }));

    expect(toast.error).toHaveBeenCalledTimes(1);
    const options = (toast.error as jest.Mock).mock.calls[0][1];
    // toastId is keyed on the query hash so repeated polling failures dedupe instead of stacking.
    expect(options.toastId).toBe('hash-1');
    expect(options.autoClose).toBe(false);
  });

  test('does not toast when the query opts out via meta.ignoreGlobalError', () => {
    handleGlobalQueryError(new Error('boom'), makeQuery({ meta: { ignoreGlobalError: true } }));

    expect(toast.error).not.toHaveBeenCalled();
  });

  test('still toasts when meta is present without the ignore flag', () => {
    handleGlobalQueryError(new Error('boom'), makeQuery({ meta: { ignoreGlobalError: false } }));

    expect(toast.error).toHaveBeenCalledTimes(1);
  });

  test('showGlobalErrorToast forwards the toastId option', () => {
    showGlobalErrorToast(new Error('x'), { toastId: 'abc' });

    expect((toast.error as jest.Mock).mock.calls[0][1].toastId).toBe('abc');
  });
});

describe('parseResponseError', () => {
  const responseOf = (status: number, body: unknown, ok = false) =>
    ({
      ok,
      status,
      json: () =>
        body === undefined ? Promise.reject(new Error('no json')) : Promise.resolve(body),
    }) as unknown as Response;

  // 권한 판정이 이 값으로 갈린다. body에서만 읽으면 ProblemDetail이 아닌 응답에서 조용히 사라진다.
  test('carries the response status for a ProblemDetail body', async () => {
    await expect(
      parseResponseError(
        responseOf(403, { status: 403, title: 'Forbidden', detail: 'Access denied' }),
      ),
    ).rejects.toMatchObject({ status: 403, message: 'Access denied' });
  });

  test('carries the response status for a body that is not a ProblemDetail', async () => {
    await expect(parseResponseError(responseOf(403, { message: 'nope' }))).rejects.toMatchObject({
      status: 403,
      message: 'nope',
    });
  });

  test('carries the response status when the body is not JSON at all', async () => {
    await expect(parseResponseError(responseOf(403, undefined))).rejects.toMatchObject({
      status: 403,
    });
  });
});

describe('page level 403', () => {
  const store = getDefaultStore();

  beforeEach(() => {
    jest.clearAllMocks();
    store.set(forbiddenPathAtom, undefined);
    window.history.replaceState({}, '', '/inspector/app-name@TOMCAT');
  });

  // 엔드포인트는 따지지 않는다. 화면이 부르는 API 중 하나라도 막혔으면 그 화면은 온전하지 않다.
  test.each([END_POINTS.INSPECTOR_AGENT_CHART, END_POINTS.AGENT_LIST, END_POINTS.HEATMAP_APP_DATA])(
    'marks the current path forbidden for a 403 from %s',
    (endPoint) => {
      handleGlobalQueryError(forbidden(), makeQuery({ queryKey: [endPoint, ''] }));

      expect(store.get(forbiddenPathAtom)).toBe('/inspector/app-name@TOMCAT');
    },
  );

  // 403은 어디서든 토스트를 띄우지 않는다. 화면이 바뀌는 곳에서는 같은 사실을 두 번 알리는
  // 것이고, 폴링하는 화면에서는 tick마다 쌓인다.
  test('never toasts a 403', () => {
    handleGlobalQueryError(forbidden(), makeQuery({ queryKey: [END_POINTS.AGENT_LIST, ''] }));

    expect(toast.error).not.toHaveBeenCalled();
  });

  // 덮을지는 화면이 정한다. 핸들러는 어느 화면에서든 도장을 찍고, map처럼 덮으면 안 되는 화면은
  // 그 도장을 읽지 않는다(`useIsForbiddenPath`를 부르지 않는다). 그래서 핸들러가 "안내를 그리는
  // 화면이 떠 있는가"를 볼 필요가 없다 — suspense 화면은 첫 응답이 올 때 아직 commit 전이다.
  test.each([
    '/serverMap/app-name@TOMCAT',
    '/serviceMap/my-service/app-name@TOMCAT',
    '/config/alarm',
  ])('stamps %s too, leaving it to the page whether to cover', (pathname) => {
    window.history.replaceState({}, '', pathname);

    handleGlobalQueryError(forbidden(), makeQuery({ queryKey: [END_POINTS.HEATMAP_APP_DATA, ''] }));

    expect(store.get(forbiddenPathAtom)).toBe(pathname);
    expect(toast.error).not.toHaveBeenCalled();
  });

  test.each([401, 404, 500])('does not mark the path forbidden for status %s', (status) => {
    handleGlobalQueryError(
      forbidden(status),
      makeQuery({ queryKey: [END_POINTS.INSPECTOR_AGENT_CHART, ''] }),
    );

    expect(store.get(forbiddenPathAtom)).toBeUndefined();
    expect(toast.error).toHaveBeenCalledTimes(1);
  });

  // 보고 있는 조회를 지우면, 에러를 그린 채 남은 컴포넌트가 다음 렌더에 새 쿼리를 만들어
  // 곧바로 다시 묻고 그것이 또 403이 되어 요청이 끝없이 반복된다. 지우는 것은 화면이 안내로
  // 바뀐 뒤다(`enterForbiddenNotice`).
  test('keeps a watched forbidden query in the cache', () => {
    const cache = queryClient.getQueryCache();
    const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=watched'];

    handleGlobalQueryError(forbidden(), cachedQuery(queryKey, 1));

    expect(cache.find({ queryKey })).toBeDefined();
    queryClient.removeQueries({ queryKey });
  });

  // 응답은 늦게 올 수 있다. 요청을 낸 화면을 떠난 뒤 도착한 403으로 새 화면을 덮으면,
  // 권한이 멀쩡한 화면이 권한 없음으로 보인다.
  test('does not cover the page for a response that arrived after leaving the screen', () => {
    handleGlobalQueryError(
      forbidden(),
      makeQuery({ queryKey: [END_POINTS.INSPECTOR_AGENT_CHART, ''], observers: 0 }),
    );

    expect(store.get(forbiddenPathAtom)).toBeUndefined();
  });

  // 떠난 화면의 답이라도 캐시에 남기면 다음 화면이 그대로 받는다. 보고 있는 컴포넌트가 없으니
  // 지워도 다시 묻는 루프가 생기지 않는다.
  test('drops a late response nobody is watching from the cache', () => {
    const cache = queryClient.getQueryCache();
    const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=stale'];

    handleGlobalQueryError(forbidden(), cachedQuery(queryKey, 0));

    expect(store.get(forbiddenPathAtom)).toBeUndefined();
    expect(cache.find({ queryKey })).toBeUndefined();
  });

  // meta.ignoreGlobalError는 토스트를 끄는 표시일 뿐, 페이지 판정까지 끄지는 않는다.
  test('marks the path forbidden even when the query opts out of the global toast', () => {
    handleGlobalQueryError(
      forbidden(),
      makeQuery({
        queryKey: [END_POINTS.INSPECTOR_AGENT_CHART, ''],
        meta: { ignoreGlobalError: true },
      }),
    );

    expect(store.get(forbiddenPathAtom)).toBe('/inspector/app-name@TOMCAT');
  });
});

// 화면이 안내로 바뀐 뒤 부른다. 여러 화면이 같은 조회를 공유하므로(사이드바의 agent 목록)
// 에러가 캐시에 남으면 다음 화면은 재조회 없이 캐시의 에러를 렌더 중에 던져 덮이지 않는다.
describe('enterForbiddenNotice', () => {
  const failedQuery = (queryKey: unknown[], observers: number, error: Error) => {
    const query = cachedQuery(queryKey, observers);
    query.setState({ ...query.state, status: 'error', error });
    return query;
  };

  beforeEach(() => {
    window.history.replaceState({}, '', '/inspector/app-name@TOMCAT');
  });

  test('drops a 403 query nobody is watching', () => {
    const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=unwatched'];
    failedQuery(queryKey, 0, forbidden());

    enterForbiddenNotice()();

    expect(queryClient.getQueryCache().find({ queryKey })).toBeUndefined();
  });

  // 덮이지 않는 곳(헤더 등)이 보고 있는 조회를 지우면 곧바로 다시 묻는 반복이 생긴다.
  test('keeps a 403 query that is still watched', () => {
    const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=still-watched'];
    failedQuery(queryKey, 1, forbidden());

    enterForbiddenNotice()();

    expect(queryClient.getQueryCache().find({ queryKey })).toBeDefined();
    queryClient.removeQueries({ queryKey });
  });

  test('keeps a query that failed with another status', () => {
    const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=other-status'];
    failedQuery(queryKey, 0, forbidden(500));

    enterForbiddenNotice()();

    expect(queryClient.getQueryCache().find({ queryKey })).toBeDefined();
    queryClient.removeQueries({ queryKey });
  });

  // 같은 화면에서 먼저 보낸 다른 요청이 안내가 뜬 **뒤에** 403을 받는 경우. 지금 화면의 요청이라
  // 도장을 찍는 쪽으로 판정되지만, 본문이 이미 사라져 보는 곳이 없다. 남기면 같은 조회를 쓰는
  // 다음 화면이 캐시의 에러를 그대로 받는다.
  describe('a 403 that arrives after the page has turned into the notice', () => {
    const lateResponse = async (queryKey: string[]) => {
      let reject: (error: Error) => void = () => {};
      const pending = queryClient
        .fetchQuery({ queryKey, queryFn: () => new Promise((_resolve, rej) => (reject = rej)) })
        .catch(() => undefined);
      return {
        deny: async () => {
          reject(forbidden());
          await pending;
        },
      };
    };

    test('is dropped from the cache while the notice is shown', async () => {
      const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=late-after-notice'];
      const late = await lateResponse(queryKey);
      const leaveNotice = enterForbiddenNotice();

      await late.deny();

      expect(queryClient.getQueryCache().find({ queryKey })).toBeUndefined();
      leaveNotice();
    });

    // 안내가 뜨기 전(suspense 조회의 첫 응답)에 지우면 suspend된 컴포넌트가 새 쿼리로 또 묻는다.
    test('is kept before the notice is shown', async () => {
      const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=late-before-notice'];
      const late = await lateResponse(queryKey);

      await late.deny();

      expect(queryClient.getQueryCache().find({ queryKey })).toBeDefined();
      queryClient.removeQueries({ queryKey });
    });

    test('is kept once the notice is gone', async () => {
      const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=late-notice-gone'];
      const late = await lateResponse(queryKey);
      enterForbiddenNotice()();

      await late.deny();

      expect(queryClient.getQueryCache().find({ queryKey })).toBeDefined();
      queryClient.removeQueries({ queryKey });
    });
  });
});

describe('retryUnlessForbidden', () => {
  // 전역 핸들러는 재시도를 다 마친 뒤에 불리므로, 403을 재시도하면 막힌 요청이 더 나가고
  // 권한 없음 안내가 그만큼 늦게 뜬다.
  test('never retries a 403', () => {
    expect(retryUnlessForbidden(0, forbidden())).toBe(false);
  });

  test('retries other failures up to the react-query default of three times', () => {
    expect(retryUnlessForbidden(0, forbidden(500))).toBe(true);
    expect(retryUnlessForbidden(2, forbidden(500))).toBe(true);
    expect(retryUnlessForbidden(3, forbidden(500))).toBe(false);
    expect(retryUnlessForbidden(0, new Error('network'))).toBe(true);
  });

  test('is the default retry policy of the shared client', () => {
    expect(queryClient.getDefaultOptions().queries?.retry).toBe(retryUnlessForbidden);
  });
});

describe('queryFn', () => {
  const fetchMock = jest.fn(() =>
    Promise.resolve({ ok: true, json: () => Promise.resolve({}) } as Response),
  );

  beforeEach(() => {
    fetchMock.mockClear();
    global.fetch = fetchMock as unknown as typeof fetch;
  });

  // 헤더를 붙이는 것은 fetch 인터셉터의 몫이다. 여기서 빈 init을 넘기면 그 판단을 흐린다.
  test('requests without an init when no service is given', async () => {
    await queryFn('/api/getApdexScore')();

    expect(fetchMock).toHaveBeenCalledWith('/api/getApdexScore');
  });

  // 조회 대상이 화면과 다른 service에 속할 때 그 service로 나가야 한다.
  test('carries the given service in the request header', async () => {
    await queryFn('/api/getApdexScore', { serviceName: 'other-service' })();

    const [, init] = fetchMock.mock.calls[0] as unknown as [string, RequestInit];
    expect(new Headers(init.headers).get(SERVICE_NAME_HEADER)).toBe('other-service');
  });
});

describe('serviceScopedQueryKeyHashFn', () => {
  const store = getDefaultStore();

  const configWithServiceMap = (enable: boolean) =>
    ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

  beforeEach(() => {
    store.set(configurationAtom, configWithServiceMap(true));
    store.set(selectedServiceAtom, 'DEFAULT');
    window.history.replaceState({}, '', '/serviceMap');
  });

  test('hashes the same queryKey differently per requested service', () => {
    store.set(selectedServiceAtom, 'service-a');
    const hashOfA = serviceScopedQueryKeyHashFn(['/api/agents/search-application']);

    store.set(selectedServiceAtom, 'service-b');
    const hashOfB = serviceScopedQueryKeyHashFn(['/api/agents/search-application']);

    expect(hashOfA).not.toBe(hashOfB);
  });

  test('hashes the servermap path the same as any other page', () => {
    // ServerMap도 service 예외가 아니므로, 같은 service라면 페이지가 달라도 같은 캐시를 쓴다.
    store.set(selectedServiceAtom, 'service-a');
    const hashOnServiceMap = serviceScopedQueryKeyHashFn(['/api/agents/search-application']);

    window.history.replaceState({}, '', '/serverMap/app-name@TOMCAT');

    expect(serviceScopedQueryKeyHashFn(['/api/agents/search-application'])).toBe(hashOnServiceMap);
  });

  test.each([END_POINTS.CONFIGURATION, END_POINTS.SERVICES, END_POINTS.SERVER_TIME])(
    'does not scope %s by service',
    (endPoint) => {
      // configuration이 service별로 나뉘면 service를 바꿀 때마다 다시 로딩되고,
      // 그동안 InitialFetchOutlet이 자식을 렌더하지 않아 화면이 사라진다.
      store.set(selectedServiceAtom, 'service-a');
      const hashOnServiceA = serviceScopedQueryKeyHashFn([endPoint]);

      store.set(selectedServiceAtom, 'service-b');

      expect(serviceScopedQueryKeyHashFn([endPoint])).toBe(hashOnServiceA);
    },
  );

  // 설정이 꺼져 있으면 헤더도 실리지 않아 모든 요청이 기본 service의 조회다. 나눌 기준이
  // 없으므로 해시에 덧붙이지 않는다 — 덧붙이면 같은 데이터를 전역 선택값마다 다시 받는다.
  test('does not scope by service when enableServiceMap is off', () => {
    store.set(configurationAtom, configWithServiceMap(false));

    store.set(selectedServiceAtom, 'service-a');
    const hashOfA = serviceScopedQueryKeyHashFn(['/api/agents/search-application']);

    store.set(selectedServiceAtom, 'service-b');

    expect(serviceScopedQueryKeyHashFn(['/api/agents/search-application'])).toBe(hashOfA);
  });

  test('keeps each service cache separate for one queryKey', () => {
    const client = new QueryClient({
      defaultOptions: { queries: { queryKeyHashFn: serviceScopedQueryKeyHashFn } },
    });
    const queryKey = ['/api/agents/search-application'];

    store.set(selectedServiceAtom, 'service-a');
    client.setQueryData(queryKey, 'service-a-data');

    // 같은 queryKey지만 다른 service에서는 캐시가 비어 있어야 한다.
    store.set(selectedServiceAtom, 'service-b');
    expect(client.getQueryData(queryKey)).toBeUndefined();

    client.setQueryData(queryKey, 'service-b-data');
    expect(client.getQueryData(queryKey)).toBe('service-b-data');

    store.set(selectedServiceAtom, 'service-a');
    expect(client.getQueryData(queryKey)).toBe('service-a-data');
  });
});
