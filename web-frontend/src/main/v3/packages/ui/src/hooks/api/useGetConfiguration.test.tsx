import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

const mockOnQueryError = jest.fn();

// reactQueryHelper transitively imports the ECharts (ESM) stack via ErrorToast.
// Expose a plain QueryClient so the cache shared by the hook and the loader path stays real,
// with the same meta-aware error handler the real queryCache uses for the global error toast.
jest.mock('./reactQueryHelper', () => {
  const { QueryClient: ActualQueryClient, QueryCache } =
    jest.requireActual('@tanstack/react-query');
  return {
    queryClient: new ActualQueryClient({
      queryCache: new QueryCache({
        onError: (error: unknown, query: { meta?: { ignoreGlobalError?: boolean } }) => {
          if (query.meta?.ignoreGlobalError) return;
          mockOnQueryError(error);
        },
      }),
      defaultOptions: { queries: { retry: false } },
    }),
  };
});

import { getConfiguration, useGetConfiguration } from './useGetConfiguration';
import { queryClient } from './reactQueryHelper';

const createWrapper = (client: QueryClient) =>
  function Wrapper({ children }: { children: React.ReactNode }) {
    return <QueryClientProvider client={client}>{children}</QueryClientProvider>;
  };

describe('useGetConfiguration', () => {
  beforeEach(() => {
    global.fetch = jest.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ 'periodMax.inspector': 42 }),
    });
  });

  afterEach(() => {
    queryClient.clear();
    mockOnQueryError.mockClear();
    jest.clearAllMocks();
  });

  test('getConfiguration requests the configuration endpoint', async () => {
    await expect(getConfiguration()).resolves.toEqual({ 'periodMax.inspector': 42 });
    expect(global.fetch).toHaveBeenCalledWith(END_POINTS.CONFIGURATION);
  });

  test('getConfiguration fetches once no matter how many route loaders ask for it', async () => {
    await getConfiguration();
    await getConfiguration();
    await getConfiguration();

    expect(global.fetch).toHaveBeenCalledTimes(1);
  });

  test('the hook reuses the value the loader already fetched', async () => {
    await getConfiguration();

    const { result } = renderHook(() => useGetConfiguration(), {
      wrapper: createWrapper(queryClient),
    });

    await waitFor(() => expect(result.current.data).toEqual({ 'periodMax.inspector': 42 }));
    expect(global.fetch).toHaveBeenCalledTimes(1);
  });

  test('the hook does not refetch when it remounts on page navigation', async () => {
    const wrapper = createWrapper(queryClient);
    const first = renderHook(() => useGetConfiguration(), { wrapper });
    await waitFor(() => expect(first.result.current.data).toBeDefined());
    first.unmount();

    const second = renderHook(() => useGetConfiguration(), { wrapper });
    await waitFor(() => expect(second.result.current.data).toBeDefined());

    expect(global.fetch).toHaveBeenCalledTimes(1);
  });

  test('a failed fetch is not cached, so the next attempt retries the request', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      json: async () => ({ message: 'backend down' }),
    });

    await expect(getConfiguration()).rejects.toThrow('backend down');
    await expect(getConfiguration()).resolves.toEqual({ 'periodMax.inspector': 42 });
    expect(global.fetch).toHaveBeenCalledTimes(2);
  });

  test('a loader failure stays silent while the hook failure still reports globally', async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: false,
      json: async () => ({ message: 'backend down' }),
    });

    // 로더 경로: catch 해서 기본값으로 진행하므로 글로벌 에러 토스트를 띄우지 않는다.
    await expect(getConfiguration()).rejects.toThrow('backend down');
    expect(mockOnQueryError).not.toHaveBeenCalled();

    // 훅 경로: 화면이 실제로 configuration을 필요로 하므로 사용자에게 알린다.
    const { result } = renderHook(() => useGetConfiguration(), {
      wrapper: createWrapper(queryClient),
    });

    await waitFor(() => expect(result.current.error).toBeTruthy());
    expect(mockOnQueryError).toHaveBeenCalledTimes(1);
  });
});
