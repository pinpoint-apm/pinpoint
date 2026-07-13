import React from 'react';
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { usePatchOtlpMetricDefUserDefined } from './usePatchOtlpMetricDefUserDefined';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('usePatchOtlpMetricDefUserDefined', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('PATCHes the params to the user-defined metric def endpoint', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'SUCCESS' }),
    });

    const { result } = renderHook(() => usePatchOtlpMetricDefUserDefined(), {
      wrapper: createWrapper(),
    });

    const params = { metricGroupName: 'g1' } as never;
    const data = await result.current.mutateAsync(params);

    expect(data).toEqual({ result: 'SUCCESS' });
    expect(global.fetch).toHaveBeenCalledWith(
      END_POINTS.OTLP_METRIC_DEF_USER_DEFINED,
      expect.objectContaining({
        method: 'PATCH',
        body: JSON.stringify(params),
        headers: { 'Content-Type': 'application/json' },
      }),
    );
  });

  test('rejects when the fetch itself fails', async () => {
    (global.fetch as jest.Mock).mockRejectedValueOnce(new Error('network down'));

    const { result } = renderHook(() => usePatchOtlpMetricDefUserDefined(), {
      wrapper: createWrapper(),
    });

    await expect(result.current.mutateAsync({} as never)).rejects.toThrow('network down');
  });
});
