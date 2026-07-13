import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { usePostService } from './usePostService';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('usePostService', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('POSTs the body to the services endpoint and resolves on SUCCESS', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'SUCCESS' }),
    });

    const { result } = renderHook(() => usePostService(), { wrapper: createWrapper() });

    const body = { serviceName: 'my-service' } as never;
    const data = await result.current.mutateAsync(body);

    expect(data).toEqual({ result: 'SUCCESS' });
    expect(global.fetch).toHaveBeenCalledWith(
      END_POINTS.SERVICES,
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(body),
        headers: { 'Content-Type': 'application/json' },
      }),
    );
  });

  test('rejects when the HTTP response is not ok', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      json: async () => ({ result: 'FAIL', message: 'boom' }),
    });

    const { result } = renderHook(() => usePostService(), { wrapper: createWrapper() });

    await expect(result.current.mutateAsync({ serviceName: 'x' } as never)).rejects.toEqual({
      result: 'FAIL',
      message: 'boom',
    });
  });

  test('rejects when the payload result is not SUCCESS even if the response is ok', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'FAIL' }),
    });

    const { result } = renderHook(() => usePostService(), { wrapper: createWrapper() });

    await expect(result.current.mutateAsync({ serviceName: 'x' } as never)).rejects.toEqual({
      result: 'FAIL',
    });
    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
