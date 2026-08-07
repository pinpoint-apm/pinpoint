import React from 'react';
import { renderHook, waitFor, act } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { getDefaultStore } from 'jotai';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { selectedServiceAtom, DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms/selectedService';

// useGetApplicationList imports queryFn from reactQueryHelper, which transitively pulls in the
// ECharts ESM stack. Stub the module with the same fetch behaviour so babel-jest does not choke.
jest.mock('./reactQueryHelper', () => ({
  queryFn: (url: string) => async () => {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Request failed with status ${response.status}.`);
    }
    return response.json();
  },
}));

import { useGetApplicationList } from './useGetApplicationList';

let testClient: QueryClient;

const createWrapper = () => {
  testClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={testClient}>{children}</QueryClientProvider>
  );
};

const okResponse = (body: unknown) => ({
  ok: true,
  status: 200,
  json: async () => body,
});

describe('useGetApplicationList', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
    getDefaultStore().set(selectedServiceAtom, DEFAULT_SERVICE);
    window.history.replaceState({}, '', '/');
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('fetches the application list from the applications endpoint', async () => {
    (global.fetch as jest.Mock).mockResolvedValue(okResponse([{ applicationName: 'A' }]));

    const { result } = renderHook(() => useGetApplicationList(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect((global.fetch as jest.Mock).mock.calls[0][0]).toBe(END_POINTS.APPLICATION_LIST);
    expect(result.current.data).toEqual([{ applicationName: 'A' }]);
  });

  test('leaves ETag revalidation to the browser HTTP cache', async () => {
    // 백엔드가 Vary + no-cache를 보내므로 훅이 If-None-Match를 직접 실을 필요가 없다.
    // 브라우저 캐시를 끄는 cache: 'no-store'도 붙이지 않아야 자동 재검증이 동작한다.
    (global.fetch as jest.Mock).mockResolvedValue(okResponse([{ applicationName: 'A' }]));

    const { result } = renderHook(() => useGetApplicationList(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    await act(async () => {
      await result.current.refetch();
    });

    await waitFor(() => expect((global.fetch as jest.Mock).mock.calls.length).toBe(2));
    for (const [, init] of (global.fetch as jest.Mock).mock.calls as [
      RequestInfo | URL,
      RequestInit | undefined,
    ][]) {
      // init.headers는 평범한 객체일 수도, Headers 인스턴스일 수도 있다(fetch 인터셉터가 후자로
      // 만든다). Headers에 대괄호 접근은 항상 undefined라 검사가 무의미해지므로 형태를 통일한다.
      expect(new Headers(init?.headers).get('If-None-Match')).toBeNull();
      expect(init?.cache).toBeUndefined();
    }
  });

  test('refetchWithClearCache requests the endpoint with clearCache=true', async () => {
    (global.fetch as jest.Mock).mockResolvedValue(okResponse([{ applicationName: 'A' }]));

    const { result } = renderHook(() => useGetApplicationList(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    act(() => {
      result.current.refetchWithClearCache();
    });

    await waitFor(() =>
      expect(
        (global.fetch as jest.Mock).mock.calls.some((c) =>
          (c[0] as string).includes('clearCache=true'),
        ),
      ).toBe(true),
    );
  });

  test('does not fetch when shouldFetch is false', async () => {
    renderHook(() => useGetApplicationList(false), { wrapper: createWrapper() });

    await act(async () => {
      await Promise.resolve();
    });

    expect(global.fetch).not.toHaveBeenCalled();
  });

  test('refetches the list when the selected service changes', async () => {
    (global.fetch as jest.Mock)
      .mockResolvedValueOnce(okResponse([{ applicationName: 'A' }]))
      .mockResolvedValueOnce(okResponse([{ applicationName: 'B' }]));

    const { result } = renderHook(() => useGetApplicationList(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual([{ applicationName: 'A' }]);

    act(() => {
      getDefaultStore().set(selectedServiceAtom, 'service-b');
    });

    await waitFor(() => expect((global.fetch as jest.Mock).mock.calls.length).toBe(2));
    await waitFor(() => expect(result.current.data).toEqual([{ applicationName: 'B' }]));
  });

  test('caches under the selected service, servermap included', async () => {
    // ServerMap도 예외가 아니라 선택된 service로 조회되므로, 그 service 캐시를 써야 한다.
    window.history.replaceState({}, '', '/serverMap/app-name@TOMCAT');
    getDefaultStore().set(selectedServiceAtom, 'service-b');
    (global.fetch as jest.Mock).mockResolvedValue(okResponse([{ applicationName: 'A' }]));

    const { result } = renderHook(() => useGetApplicationList(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(testClient.getQueryData([END_POINTS.APPLICATION_LIST, 'service-b'])).toEqual([
      { applicationName: 'A' },
    ]);
  });
});
