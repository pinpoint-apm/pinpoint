import { getDefaultStore } from 'jotai';
import { configurationAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';
import {
  installServiceNameFetchInterceptor,
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
    window.fetch = originalFetch as typeof window.fetch;
    installServiceNameFetchInterceptor();
  });

  beforeEach(() => {
    originalFetch.mockClear();
    store.set(configurationAtom, undefined);
    store.set(selectedServiceAtom, 'DEFAULT');
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
});
