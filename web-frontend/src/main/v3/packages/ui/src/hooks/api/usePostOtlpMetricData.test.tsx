import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

jest.mock('./reactQueryHelper', () => ({
  parseResponseError: jest.fn(async () => {
    throw new Error('parsed error');
  }),
}));

import { usePostOtlpMetricData } from './usePostOtlpMetricData';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('usePostOtlpMetricData', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('POSTs the body as JSON to the otlp metric data endpoint', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ metricValues: [] }),
    });

    const { result } = renderHook(() => usePostOtlpMetricData(), { wrapper: createWrapper() });

    const body = { metricGroupName: 'g1' } as never;
    const data = await result.current.mutateAsync(body);

    expect(data).toEqual({ metricValues: [] });
    expect(global.fetch).toHaveBeenCalledWith(
      END_POINTS.OTLP_METRIC_DATA,
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(body),
        headers: { 'Content-Type': 'application/json' },
      }),
    );
  });

  test('rejects through parseResponseError when the response is not ok', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: false, json: async () => ({}) });

    const { result } = renderHook(() => usePostOtlpMetricData(), { wrapper: createWrapper() });

    await expect(result.current.mutateAsync({} as never)).rejects.toThrow('parsed error');
    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
