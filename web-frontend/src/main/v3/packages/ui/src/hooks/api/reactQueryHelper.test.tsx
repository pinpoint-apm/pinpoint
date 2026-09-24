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
  forbiddenNoticeMountCountAtom,
  forbiddenPathAtom,
  selectedServiceAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { Configuration, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { SERVICE_NAME_HEADER } from './serviceNameFetchInterceptor';
import {
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
    // 기본은 안내를 그리는 화면(`useIsForbiddenPath`를 부르는 페이지)이 떠 있는 상태다.
    store.set(forbiddenNoticeMountCountAtom, 1);
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

  // map API에는 권한 검사가 없다. 403은 고른 노드를 묻는 조회에서 나오므로 map은 남겨야 한다.
  test.each([
    '/serverMap/app-name@TOMCAT',
    '/serviceMap/my-service/app-name@TOMCAT',
    '/filteredMap/my-service/app-name@TOMCAT',
  ])('does not cover the map page %s', (pathname) => {
    window.history.replaceState({}, '', pathname);

    handleGlobalQueryError(forbidden(), makeQuery({ queryKey: [END_POINTS.HEATMAP_APP_DATA, ''] }));

    expect(store.get(forbiddenPathAtom)).toBeUndefined();
    // 덮지 않더라도 토스트는 띄우지 않는다. 위젯의 인라인 안내가 어디가 막혔는지 가리킨다.
    expect(toast.error).not.toHaveBeenCalled();
  });

  // 같은 엔드포인트라도 어느 화면에서 불렀는지로 갈린다.
  test('covers the full screen page for the same endpoint', () => {
    window.history.replaceState({}, '', '/heatmapFullScreenMode/app-name@TOMCAT');

    handleGlobalQueryError(forbidden(), makeQuery({ queryKey: [END_POINTS.HEATMAP_APP_DATA, ''] }));

    expect(store.get(forbiddenPathAtom)).toBe('/heatmapFullScreenMode/app-name@TOMCAT');
  });

  test.each([401, 404, 500])('does not mark the path forbidden for status %s', (status) => {
    handleGlobalQueryError(
      forbidden(status),
      makeQuery({ queryKey: [END_POINTS.INSPECTOR_AGENT_CHART, ''] }),
    );

    expect(store.get(forbiddenPathAtom)).toBeUndefined();
    expect(toast.error).toHaveBeenCalledTimes(1);
  });

  // 여러 화면이 같은 조회를 공유한다(사이드바의 agent 목록). 에러가 캐시에 남으면 두 번째
  // 화면은 재조회 없이 캐시의 에러를 렌더 중에 던져, 이 핸들러가 불리지 않아 덮이지 않는다.
  test('drops the forbidden query from the cache so the next screen asks again', () => {
    const cache = queryClient.getQueryCache();
    const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=myApp'];

    handleGlobalQueryError(forbidden(), cachedQuery(queryKey, 1));

    expect(cache.find({ queryKey })).toBeUndefined();
  });

  // 덮지 않는 화면에서 지우면, 에러를 그린 채 남은 컴포넌트가 다음 렌더에 새 쿼리를 만들어
  // 곧바로 다시 묻고 그것이 또 403이 되어 요청이 끝없이 반복된다.
  test('keeps the forbidden query in the cache while a map page is still watching it', () => {
    window.history.replaceState({}, '', '/serverMap/app-name@TOMCAT');
    const cache = queryClient.getQueryCache();
    const queryKey = [END_POINTS.HEATMAP_APP_DATA, '?applicationName=myApp'];

    handleGlobalQueryError(forbidden(), cachedQuery(queryKey, 1));

    expect(cache.find({ queryKey })).toBeDefined();
    queryClient.removeQueries({ queryKey });
  });

  test('keeps a query that failed with another status in the cache', () => {
    const cache = queryClient.getQueryCache();
    const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=other'];

    handleGlobalQueryError(forbidden(500), cachedQuery(queryKey, 1));

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
  test('still drops a query nobody is watching from the cache', () => {
    const cache = queryClient.getQueryCache();
    const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=stale'];

    handleGlobalQueryError(forbidden(), cachedQuery(queryKey, 0));

    expect(cache.find({ queryKey })).toBeUndefined();
  });

  // map 화면을 떠난 뒤 도착한 경우. 덮지도 않고, 보고 있는 컴포넌트도 없으니 지운다.
  test('neither covers nor keeps a late response on a map page nobody is watching', () => {
    window.history.replaceState({}, '', '/serverMap/app-name@TOMCAT');
    const cache = queryClient.getQueryCache();
    const queryKey = [END_POINTS.HEATMAP_APP_DATA, '?applicationName=left'];

    handleGlobalQueryError(forbidden(), cachedQuery(queryKey, 0));

    expect(store.get(forbiddenPathAtom)).toBeUndefined();
    expect(cache.find({ queryKey })).toBeUndefined();
  });

  // 안내를 그리지 않는 화면(설정의 Alarm 등)은 경로로는 덮을 화면이어도 본문이 그대로 남는다.
  // 그 상태에서 보고 있는 조회를 지우면 남은 컴포넌트가 곧바로 다시 묻고 또 403을 받는다.
  describe('on a page that does not render the notice', () => {
    beforeEach(() => {
      store.set(forbiddenNoticeMountCountAtom, 0);
      window.history.replaceState({}, '', '/config/alarm');
    });

    test('does not mark the path forbidden', () => {
      handleGlobalQueryError(forbidden(), makeQuery({ queryKey: [END_POINTS.AGENT_LIST, ''] }));

      expect(store.get(forbiddenPathAtom)).toBeUndefined();
    });

    test('keeps the forbidden query in the cache while it is still watched', () => {
      const cache = queryClient.getQueryCache();
      const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=unguarded'];

      handleGlobalQueryError(forbidden(), cachedQuery(queryKey, 1));

      expect(cache.find({ queryKey })).toBeDefined();
      queryClient.removeQueries({ queryKey });
    });

    // 보고 있는 컴포넌트가 없으면 다시 묻는 루프가 생기지 않으므로, 다음 화면을 위해 지운다.
    test('still drops a query nobody is watching from the cache', () => {
      const cache = queryClient.getQueryCache();
      const queryKey = [END_POINTS.AGENT_LIST, '?applicationName=unguarded-left'];

      handleGlobalQueryError(forbidden(), cachedQuery(queryKey, 0));

      expect(cache.find({ queryKey })).toBeUndefined();
    });
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
