import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

// reactQueryHelper transitively imports the ECharts ESM stack, which babel-jest cannot parse.
// Only queryFn is needed here, so stub it with a plain fetch caller.
jest.mock('./reactQueryHelper', () => ({
  queryFn: (url: string) => async () => {
    const response = await global.fetch(url);
    return response.json();
  },
}));

import { useGetServiceMap } from './useGetServiceMap';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

const RANGE = { from: '2023-11-10-14-30-00', to: '2023-11-10-15-00-00' };

describe('useGetServiceMap', () => {
  beforeEach(() => {
    global.fetch = jest
      .fn()
      .mockResolvedValue({ ok: true, status: 200, json: async () => ({}) } as never);
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('fetches when an application is given', async () => {
    const { result } = renderHook(
      () => useGetServiceMap({ applicationName: 'TestApp', ...RANGE }),
      { wrapper: createWrapper() },
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect((global.fetch as jest.Mock).mock.calls[0][0]).toContain(END_POINTS.SERVICE_MAP_DATA);
    expect((global.fetch as jest.Mock).mock.calls[0][0]).toContain('applicationName=TestApp');
  });

  // DEFAULT service는 application을 골라야만 조회한다.
  test('stays disabled without an application by default', () => {
    const { result } = renderHook(() => useGetServiceMap({ ...RANGE }), {
      wrapper: createWrapper(),
    });

    expect(result.current.fetchStatus).toBe('idle');
    expect(global.fetch).not.toHaveBeenCalled();
  });

  // DEFAULT가 아닌 service는 백엔드가 소속 application을 모두 모아 map을 그린다.
  test('fetches without an application when requiresApplication is false', async () => {
    const { result } = renderHook(
      () => useGetServiceMap({ ...RANGE }, { requiresApplication: false }),
      { wrapper: createWrapper() },
    );

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect((global.fetch as jest.Mock).mock.calls[0][0]).not.toContain('applicationName');
  });

  test('stays disabled without a date range even when the application is optional', () => {
    const { result } = renderHook(
      () => useGetServiceMap({ from: '', to: '' }, { requiresApplication: false }),
      { wrapper: createWrapper() },
    );

    expect(result.current.fetchStatus).toBe('idle');
    expect(global.fetch).not.toHaveBeenCalled();
  });
});
