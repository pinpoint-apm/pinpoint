import React from 'react';
import { renderHook, waitFor, act } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { getDefaultStore } from 'jotai';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { selectedServiceAtom, DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms/selectedService';

// useGetApplicationList reads the module-level queryClient (from reactQueryHelper,
// which transitively imports the ECharts ESM stack) only for the 304 cache lookup.
// Mock it so babel-jest does not choke on echarts and so we can drive the 304 path.
const mockGetQueryData = jest.fn();
jest.mock('./reactQueryHelper', () => ({
  queryClient: { getQueryData: (...args: unknown[]) => mockGetQueryData(...args) },
}));

import { useGetApplicationList } from './useGetApplicationList';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

const okResponse = (body: unknown, etag?: string) => ({
  ok: true,
  status: 200,
  headers: { get: (key: string) => (key === 'ETag' ? (etag ?? null) : null) },
  json: async () => body,
});

describe('useGetApplicationList', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
    mockGetQueryData.mockReset();
    getDefaultStore().set(selectedServiceAtom, DEFAULT_SERVICE);
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('fetches the application list from the applications endpoint', async () => {
    (global.fetch as jest.Mock).mockResolvedValue(okResponse([{ applicationName: 'A' }], 'v1'));

    const { result } = renderHook(() => useGetApplicationList(), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect((global.fetch as jest.Mock).mock.calls[0][0]).toBe(END_POINTS.APPLICATION_LIST);
    expect(result.current.data).toEqual([{ applicationName: 'A' }]);
  });

  test('sends the cached ETag as If-None-Match and reuses cached data on 304', async () => {
    // First response establishes the ETag.
    (global.fetch as jest.Mock)
      .mockResolvedValueOnce(okResponse([{ applicationName: 'A' }], 'etag-123'))
      .mockResolvedValueOnce({ status: 304 });
    mockGetQueryData.mockReturnValue([{ applicationName: 'A' }]);

    const { result } = renderHook(() => useGetApplicationList(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    await act(async () => {
      await result.current.refetch();
    });

    await waitFor(() => expect((global.fetch as jest.Mock).mock.calls.length).toBe(2));
    const secondInit = (global.fetch as jest.Mock).mock.calls[1][1] as { headers: HeadersInit };
    expect((secondInit.headers as Record<string, string>)['If-None-Match']).toBe('etag-123');
    expect(result.current.data).toEqual([{ applicationName: 'A' }]);
  });

  test('refetchWithClearCache requests the endpoint with clearCache=true', async () => {
    (global.fetch as jest.Mock).mockResolvedValue(okResponse([{ applicationName: 'A' }], 'v1'));

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
      .mockResolvedValueOnce(okResponse([{ applicationName: 'A' }], 'etag-a'))
      .mockResolvedValueOnce(okResponse([{ applicationName: 'B' }], 'etag-b'));

    const { result } = renderHook(() => useGetApplicationList(), { wrapper: createWrapper() });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual([{ applicationName: 'A' }]);

    act(() => {
      getDefaultStore().set(selectedServiceAtom, 'service-b');
    });

    await waitFor(() => expect((global.fetch as jest.Mock).mock.calls.length).toBe(2));
    await waitFor(() => expect(result.current.data).toEqual([{ applicationName: 'B' }]));
  });
});
