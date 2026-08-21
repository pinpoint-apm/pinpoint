import { getDefaultStore } from 'jotai';
import { configurationAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';
import {
  getRequestService,
  installServiceNameFetchInterceptor,
  resolveRequestService,
  SERVICE_NAME_HEADER,
} from './serviceNameFetchInterceptor';

const store = getDefaultStore();
const originalFetch = jest.fn(() => Promise.resolve(undefined as unknown as Response));

const configWithServiceMap = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

const lastCall = () => originalFetch.mock.calls[originalFetch.mock.calls.length - 1];

const headerOfLastCall = (): string | null => {
  const [, init] = lastCall() as unknown as [RequestInfo | URL, RequestInit | undefined];
  return new Headers(init?.headers).get(SERVICE_NAME_HEADER);
};

describe('serviceNameFetchInterceptor', () => {
  beforeAll(() => {
    // 패치 대상이 될 원본 fetch를 먼저 심어두고, 인터셉터를 한 번 설치한다.
    // 인터셉터는 매 요청 시 configurationAtom에서 최신값을 직접 읽으므로 주입할 것이 없다.
    window.fetch = originalFetch as typeof window.fetch;
    installServiceNameFetchInterceptor();
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

    const [, init] = lastCall() as unknown as [RequestInfo | URL, RequestInit | undefined];
    const headers = new Headers(init?.headers);
    expect(headers.get('Content-Type')).toBe('application/json');
    expect(headers.get(SERVICE_NAME_HEADER)).toBe('my-service');
    expect(init?.method).toBe('POST');
  });

  // 조회 대상이 화면과 다른 service에 속할 때 호출자가 직접 헤더를 싣는다.
  // 인터셉터는 경로/전역 선택값만 보므로 그 사실을 알 수 없어, 실린 값을 덮어쓰면 안 된다.
  test('keeps the service header the caller already set', async () => {
    store.set(configurationAtom, configWithServiceMap(true));
    store.set(selectedServiceAtom, 'my-service');

    await window.fetch('/api/getApdexScore', {
      headers: { [SERVICE_NAME_HEADER]: 'other-service' },
    });

    expect(headerOfLastCall()).toBe('other-service');
  });

  test.each([
    ['/serverMap/app-name@TOMCAT?from=1&to=2'],
    ['/serverMap/realtime/app-name@TOMCAT'],
    ['/serviceMap/app-name@TOMCAT?from=1&to=2'],
    ['/inspector/app-name@TOMCAT'],
    ['/config/general'],
  ])('adds the header on every page when enableServiceMap is on: %s', async (pathname) => {
    store.set(configurationAtom, configWithServiceMap(true));
    store.set(selectedServiceAtom, 'my-service');
    window.history.replaceState({}, '', pathname);

    await window.fetch('/api/agents/search-application');

    expect(headerOfLastCall()).toBe('my-service');
  });

  test('sends the service name carried by the transactionList path, not the selected one', async () => {
    store.set(configurationAtom, configWithServiceMap(true));
    store.set(selectedServiceAtom, 'my-service');
    window.history.replaceState({}, '', '/transactionList/url-service/app-name@TOMCAT?from=1&to=2');

    await window.fetch('/api/transactionmetadata');

    expect(headerOfLastCall()).toBe('url-service');
  });

  test('falls back to the selected service on a legacy transactionList path', async () => {
    store.set(configurationAtom, configWithServiceMap(true));
    store.set(selectedServiceAtom, 'my-service');
    window.history.replaceState({}, '', '/transactionList/app-name@TOMCAT?from=1&to=2');

    await window.fetch('/api/transactionmetadata');

    expect(headerOfLastCall()).toBe('my-service');
  });

  describe('resolveRequestService', () => {
    test('resolves to the selected service on a service path', () => {
      window.history.replaceState({}, '', '/serviceMap/app-name@TOMCAT');

      expect(resolveRequestService('my-service')).toBe('my-service');
    });

    test('prefers the service name carried by the path', () => {
      // 헤더와 캐시 키가 같은 값에서 파생되도록, URL에 실린 serviceName을 전역 선택값보다 앞세운다.
      window.history.replaceState({}, '', '/transactionList/url-service/app-name@TOMCAT');

      expect(resolveRequestService('my-service')).toBe('url-service');
    });

    test('ignores the leading segment on paths that do not carry a service name', () => {
      window.history.replaceState({}, '', '/inspector/svc@app-name@TOMCAT');

      expect(resolveRequestService('my-service')).toBe('my-service');
    });

    test('resolves to the selected service on the servermap page too', () => {
      // ServerMap도 예외가 아니다. 헤더가 선택된 service로 나가므로 캐시 키도 같아야 한다.
      window.history.replaceState({}, '', '/serverMap/app-name@TOMCAT');

      expect(resolveRequestService('my-service')).toBe('my-service');
    });

    test('getRequestService reads the currently selected service from the store', () => {
      store.set(selectedServiceAtom, 'my-service');
      window.history.replaceState({}, '', '/serviceMap/app-name@TOMCAT');
      expect(getRequestService()).toBe('my-service');

      window.history.replaceState({}, '', '/serverMap/app-name@TOMCAT');
      expect(getRequestService()).toBe('my-service');
    });
  });
});
