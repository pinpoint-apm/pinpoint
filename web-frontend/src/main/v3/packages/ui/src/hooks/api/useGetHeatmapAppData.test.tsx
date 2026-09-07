import React from 'react';
import { renderHook, act } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

// reactQueryHelper는 ErrorToast를 통해 ECharts(ESM)까지 끌어오는데 babel-jest가 변환하지 않는다.
// 같은 동작의 fetch 구현으로 대체해 이 테스트는 요청 여부/URL만 본다.
const mockQueryFn = jest.fn();
jest.mock('./reactQueryHelper', () => ({
  queryFn: (url: string, options?: { serviceName?: string }) => mockQueryFn(url, options),
}));

import { useGetHeatmapAppData } from './useGetHeatmapAppData';

const createWrapper = () => {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

const range = { from: '20260907T000000Z', to: '20260907T002000Z' };

describe('useGetHeatmapAppData', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
    mockQueryFn.mockImplementation((url: string) => async () => {
      const response = await fetch(url);
      return response.json();
    });
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  const flush = async () => {
    await act(async () => {
      await Promise.resolve();
    });
  };

  // 이 API는 applicationName이 필수다. 조회 대상이 정해지기 전에 부르면
  // 400 "Required parameter 'applicationName' is not present." 가 온다. (이슈 #10587)
  test('does not fetch until the target application is known', async () => {
    renderHook(() => useGetHeatmapAppData({ ...range }), { wrapper: createWrapper() });
    await flush();

    expect(global.fetch).not.toHaveBeenCalled();
  });

  test('does not fetch when only the application name is known', async () => {
    renderHook(() => useGetHeatmapAppData({ ...range, applicationName: 'app-a' }), {
      wrapper: createWrapper(),
    });
    await flush();

    expect(global.fetch).not.toHaveBeenCalled();
  });

  test('fetches once the application and its service type are known', async () => {
    renderHook(
      () => useGetHeatmapAppData({ ...range, applicationName: 'app-a', serviceTypeName: 'TOMCAT' }),
      { wrapper: createWrapper() },
    );
    await flush();

    expect(global.fetch).toHaveBeenCalledTimes(1);
    expect((global.fetch as jest.Mock).mock.calls[0][0]).toContain(END_POINTS.HEATMAP_APP_DATA);
    expect((global.fetch as jest.Mock).mock.calls[0][0]).toContain('applicationName=app-a');
  });

  test('carries the given service on the request', async () => {
    renderHook(
      () =>
        useGetHeatmapAppData(
          { ...range, applicationName: 'b-1', serviceTypeName: 'TOMCAT' },
          'bService',
        ),
      { wrapper: createWrapper() },
    );
    await flush();

    expect(mockQueryFn).toHaveBeenCalledWith(expect.stringContaining('applicationName=b-1'), {
      serviceName: 'bService',
    });
  });
});
