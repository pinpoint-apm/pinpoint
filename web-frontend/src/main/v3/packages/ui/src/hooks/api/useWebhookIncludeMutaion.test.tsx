import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { useWebhookIncludeMutaion } from './useWebhookIncludeMutaion';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('useWebhookIncludeMutaion', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('sends the request with the given method and body to the include-webhook endpoint', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'SUCCESS' }),
    });

    const { result } = renderHook(() => useWebhookIncludeMutaion(), { wrapper: createWrapper() });

    const params = { ruleId: 'r1', webhookIds: ['w1'] } as never;
    const data = await result.current.mutateAsync({ params, method: 'POST' });

    expect(data).toEqual({ result: 'SUCCESS' });
    expect(global.fetch).toHaveBeenCalledWith(
      END_POINTS.INCLUDE_WEBHOOK,
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(params),
        headers: { 'Content-Type': 'application/json' },
      }),
    );
  });

  test('rejects when the payload result is not SUCCESS', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'FAIL' }),
    });

    const { result } = renderHook(() => useWebhookIncludeMutaion(), { wrapper: createWrapper() });

    await expect(
      result.current.mutateAsync({ params: {} as never, method: 'PUT' }),
    ).rejects.toEqual({ result: 'FAIL' });
    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
