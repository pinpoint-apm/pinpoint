import React from 'react';
import { renderHook, waitFor, act } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS, ApplicationType } from '@pinpoint-fe/ui/src/constants';

// reactQueryHelper transitively imports the ECharts (ESM) chart stack via ErrorToast,
// which babel-jest does not transform. Mock queryFn with an equivalent fetch-based
// implementation so this test can focus on the hook's URL/enabled logic.
jest.mock('./reactQueryHelper', () => ({
  queryFn: (url: string) => async () => {
    const response = await fetch(url);
    return response.json();
  },
}));

import { useGetScatterData } from './useGetScatterData';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

const application = {
  applicationName: 'TestApp',
  serviceType: 'SPRING_BOOT',
} as ApplicationType;

const dateRange = {
  isRealtime: false,
  from: new Date('2023-11-10T14:30:00Z'),
  to: new Date('2023-11-10T15:00:00Z'),
};

describe('useGetScatterData', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('does not fetch until the x/y group units are set', async () => {
    const { result } = renderHook(() => useGetScatterData(application, dateRange), {
      wrapper: createWrapper(),
    });

    // Give any deferred/effect updates a chance to flush.
    await act(async () => {
      await Promise.resolve();
    });

    expect(global.fetch).not.toHaveBeenCalled();
    expect(result.current.data).toBeUndefined();
  });

  test('fetches the scatter data once group units are provided', async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: true,
      json: async () => ({ complete: true }),
    });

    const { result } = renderHook(() => useGetScatterData(application, dateRange), {
      wrapper: createWrapper(),
    });

    act(() => {
      result.current.setQueryParams((prev) => ({ ...prev, xGroupUnit: 10, yGroupUnit: 100 }));
    });

    await waitFor(() => expect(global.fetch).toHaveBeenCalled());

    const calledUrl = (global.fetch as jest.Mock).mock.calls[0][0] as string;
    expect(calledUrl).toContain(END_POINTS.SCATTER_DATA);
    expect(calledUrl).toContain('application=TestApp');
    expect(calledUrl).toContain('serviceTypeName=SPRING_BOOT');
    expect(calledUrl).toContain('xGroupUnit=10');
    expect(calledUrl).toContain('yGroupUnit=100');
  });

  test('does not fetch when the application is missing even if group units are set', async () => {
    const { result } = renderHook(
      () => useGetScatterData(undefined as unknown as ApplicationType, dateRange),
      { wrapper: createWrapper() },
    );

    act(() => {
      result.current.setQueryParams((prev) => ({ ...prev, xGroupUnit: 10, yGroupUnit: 100 }));
    });

    await act(async () => {
      await Promise.resolve();
    });

    expect(global.fetch).not.toHaveBeenCalled();
  });
});
