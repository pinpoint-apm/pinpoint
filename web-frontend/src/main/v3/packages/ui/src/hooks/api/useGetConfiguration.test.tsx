import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

// reactQueryHelper는 ErrorToast를 통해 ECharts(ESM) 스택을 전이적으로 import 한다.
// 그 한 줄만 끊어 두면 실제 queryClient·queryFn·글로벌 에러 핸들러를 그대로 검증할 수 있다.
jest.mock('../../components/Error/ErrorToast', () => ({ ErrorToast: () => null }));
// 글로벌 에러 토스트가 떴는지 관찰하기 위한 목.
jest.mock('react-toastify', () => ({ toast: { error: jest.fn() } }));

import { toast } from 'react-toastify';
import { getConfiguration, useGetConfiguration } from './useGetConfiguration';
import { queryClient } from './reactQueryHelper';

// 훅의 실패를 즉시 관측하려면 재시도를 끈다. (로더 경로는 이미 retry: false)
// 나머지 기본 옵션(queryKeyHashFn 등)은 실제 설정을 유지한다.
const defaultOptions = queryClient.getDefaultOptions();
queryClient.setDefaultOptions({
  ...defaultOptions,
  queries: { ...defaultOptions.queries, retry: false },
});

const wrapper = function Wrapper({ children }: { children: React.ReactNode }) {
  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
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

    const { result } = renderHook(() => useGetConfiguration(), { wrapper });

    await waitFor(() => expect(result.current.data).toEqual({ 'periodMax.inspector': 42 }));
    expect(global.fetch).toHaveBeenCalledTimes(1);
  });

  test('the hook does not refetch when it remounts on page navigation', async () => {
    const first = renderHook(() => useGetConfiguration(), { wrapper });
    await waitFor(() => expect(first.result.current.data).toBeDefined());
    first.unmount();

    const second = renderHook(() => useGetConfiguration(), { wrapper });
    await waitFor(() => expect(second.result.current.data).toBeDefined());

    expect(global.fetch).toHaveBeenCalledTimes(1);
  });

  test('the cached configuration survives with no observer left', async () => {
    // 로더만 값을 읽고 훅이 아직 마운트되지 않은 구간에도 캐시가 수거되지 않아야 한다.
    await getConfiguration();

    expect(queryClient.getQueryData([END_POINTS.CONFIGURATION])).toEqual({
      'periodMax.inspector': 42,
    });
    const [query] = queryClient.getQueryCache().findAll({ queryKey: [END_POINTS.CONFIGURATION] });
    expect(query.gcTime).toBe(Infinity);
  });

  test('a failed fetch is not cached, so the next attempt retries the request', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: async () => ({ message: 'backend down' }),
    });

    await expect(getConfiguration()).rejects.toThrow('backend down');
    await expect(getConfiguration()).resolves.toEqual({ 'periodMax.inspector': 42 });
    expect(global.fetch).toHaveBeenCalledTimes(2);
  });

  test('a non-JSON error body still reports a readable message', async () => {
    // 백엔드가 아니라 앞단 프록시가 HTML 502를 돌려주는 경우. 공용 queryFn이 파싱 실패를
    // 흡수해 상태 코드가 담긴 메시지를 만든다.
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 502,
      json: async () => {
        throw new SyntaxError('Unexpected token <');
      },
    });

    await expect(getConfiguration()).rejects.toThrow('Request failed with status 502');
  });

  test('a ProblemDetail error keeps its fields for the error toast', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: async () => ({ status: 500, title: 'Internal Server Error', detail: 'db is down' }),
    });

    await expect(getConfiguration()).rejects.toMatchObject({
      message: 'db is down',
      status: 500,
      title: 'Internal Server Error',
    });
  });

  test('a loader failure stays silent while the hook failure still reports globally', async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({ message: 'backend down' }),
    });

    // 로더 경로: catch 해서 기본값으로 진행하므로 글로벌 에러 토스트를 띄우지 않는다.
    await expect(getConfiguration()).rejects.toThrow('backend down');
    expect(toast.error).not.toHaveBeenCalled();

    // 훅 경로: 화면이 실제로 configuration을 필요로 하므로 사용자에게 알린다.
    const { result } = renderHook(() => useGetConfiguration(), { wrapper });

    await waitFor(() => expect(result.current.error).toBeTruthy());
    expect(toast.error).toHaveBeenCalledTimes(1);
  });
});
