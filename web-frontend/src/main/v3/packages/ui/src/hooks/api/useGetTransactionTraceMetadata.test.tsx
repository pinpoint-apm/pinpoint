import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

// reactQueryHelper transitively imports the ECharts (ESM) stack via ErrorToast,
// which babel-jest does not transform. Mock queryFn with an equivalent fetch impl.
jest.mock('./reactQueryHelper', () => ({
  queryFn: (url: string) => async () => {
    const response = await fetch(url);
    return response.json();
  },
}));

import {
  getTransactionTraceMetadataQueryOptions,
  useGetTransactionTraceMetadata,
} from './useGetTransactionTraceMetadata';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('useGetTransactionTraceMetadata', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('should fetch metadata by traceId when traceId is provided', async () => {
    const response = {
      metadata: [{ traceId: 't1', applicationName: 'app', serviceType: 'OPENTELEMETRY_SERVER' }],
      complete: true,
      resultFrom: 0,
    };
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => response,
    });

    const { result } = renderHook(() => useGetTransactionTraceMetadata({ traceId: 't1' }), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual(response);
    const [url] = (global.fetch as jest.Mock).mock.calls[0];
    expect(url).toBe(`${END_POINTS.TRANSACTION_TRACE_METADATA}?traceId=t1`);
  });

  test('should not fetch when traceId is missing', () => {
    const { result } = renderHook(() => useGetTransactionTraceMetadata({}), {
      wrapper: createWrapper(),
    });

    expect(result.current.fetchStatus).toBe('idle');
    expect(global.fetch).not.toHaveBeenCalled();
  });

  test('should share the query key between the hook and click-time fetchQuery callers', () => {
    const options = getTransactionTraceMetadataQueryOptions('t1');
    expect(options.queryKey).toEqual([
      END_POINTS.TRANSACTION_TRACE_METADATA,
      '?traceId=t1',
    ]);
  });
});
