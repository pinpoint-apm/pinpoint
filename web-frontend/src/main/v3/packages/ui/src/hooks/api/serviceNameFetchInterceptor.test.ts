import { getDefaultStore } from 'jotai';
import { configurationAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';
import {
  getRequestService,
  installServiceNameFetchInterceptor,
  isServiceExcludedPath,
  resolveRequestService,
  SERVICE_NAME_HEADER,
} from './serviceNameFetchInterceptor';

const store = getDefaultStore();
const originalFetch = jest.fn(() => Promise.resolve(undefined as unknown as Response));

const configWithServiceMap = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

const lastCall = () => originalFetch.mock.calls[originalFetch.mock.calls.length - 1];

const headerOfLastCall = (): string | null => {
  const [, init] = lastCall() as [RequestInfo | URL, RequestInit | undefined];
  return new Headers(init?.headers).get(SERVICE_NAME_HEADER);
};

describe('serviceNameFetchInterceptor', () => {
  beforeAll(() => {
    // 패치 대상이 될 원본 fetch를 먼저 심어두고, 인터셉터를 한 번 설치한다.
    // 인터셉터는 매 요청 시 getter로 최신 configuration을 읽으므로, store에서 읽도록 주입한다.
    window.fetch = originalFetch as typeof window.fetch;
    installServiceNameFetchInterceptor(() => store.get(configurationAtom));
  });

  beforeEach(() => {
    originalFetch.mockClear();
    store.set(configurationAtom, undefined);
    store.set(selectedServiceAtom, 'DEFAULT');
    window.history.replaceState({}, '', '/serviceMap');
  });

  test('does not add the service header when enableServiceMap is off', async () => {
    store.set(configurationAtom, configWithServiceMap(false));
    store.set(selectedServiceAtom, 'my-service');

    await window.fetch('/api/serverMap');

    expect(originalFetch).toHaveBeenCalledTimes(1);
    expect(headerOfLastCall()).toBeNull();
  });

  test('adds the selected service header on /api requests when enableServiceMap is on', async () => {
    store.set(configurationAtom, configWithServiceMap(true));
    store.set(selectedServiceAtom, 'my-service');

    await window.fetch('/api/serverMap');

    expect(headerOfLastCall()).toBe('my-service');
  });

  test('sends DEFAULT when the selected service is the default value', async () => {
    store.set(configurationAtom, configWithServiceMap(true));

    await window.fetch('/api/configuration');

    expect(headerOfLastCall()).toBe('DEFAULT');
  });

  test('does not add the header to non-/api requests', async () => {
    store.set(configurationAtom, configWithServiceMap(true));
    store.set(selectedServiceAtom, 'my-service');

    await window.fetch('/static/logo.png');

    expect(headerOfLastCall()).toBeNull();
  });

  test('preserves existing request headers while adding the service header', async () => {
    store.set(configurationAtom, configWithServiceMap(true));
    store.set(selectedServiceAtom, 'my-service');

    await window.fetch('/api/userGroup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({}),
    });

    const [, init] = lastCall() as [RequestInfo | URL, RequestInit | undefined];
    const headers = new Headers(init?.headers);
    expect(headers.get('Content-Type')).toBe('application/json');
    expect(headers.get(SERVICE_NAME_HEADER)).toBe('my-service');
    expect(init?.method).toBe('POST');
  });

  test('does not add the header on the servermap page, even for a shared API', async () => {
    store.set(configurationAtom, configWithServiceMap(true));
    store.set(selectedServiceAtom, 'my-service');
    window.history.replaceState({}, '', '/serverMap/app-name@TOMCAT?from=1&to=2');

    await window.fetch('/api/agents/search-application');

    expect(originalFetch).toHaveBeenCalledTimes(1);
    expect(headerOfLastCall()).toBeNull();
  });

  describe('resolveRequestService', () => {
    test('resolves to the selected service on a service path', () => {
      window.history.replaceState({}, '', '/serviceMap/app-name@TOMCAT');

      expect(resolveRequestService('my-service')).toBe('my-service');
    });

    test('resolves to the default service on the servermap page', () => {
      // 헤더를 생략해 백엔드 기본 service로 응답받으므로, 캐시도 기본 service로 키를 잡아야 한다.
      window.history.replaceState({}, '', '/serverMap/app-name@TOMCAT');

      expect(resolveRequestService('my-service')).toBe('DEFAULT');
    });

    test('getRequestService reads the currently selected service from the store', () => {
      store.set(selectedServiceAtom, 'my-service');
      window.history.replaceState({}, '', '/serviceMap/app-name@TOMCAT');
      expect(getRequestService()).toBe('my-service');

      window.history.replaceState({}, '', '/serverMap/app-name@TOMCAT');
      expect(getRequestService()).toBe('DEFAULT');
    });
  });

  describe('isServiceExcludedPath', () => {
    test.each([
      ['/serverMap', true],
      ['/serverMap/app-name@TOMCAT', true],
      ['/serverMap/realtime/app-name@TOMCAT', true],
      ['/serviceMap/app-name@TOMCAT', false],
      ['/inspector', false],
    ])('returns %p → %p', (pathname, expected) => {
      expect(isServiceExcludedPath(pathname)).toBe(expected);
    });
  });
});
