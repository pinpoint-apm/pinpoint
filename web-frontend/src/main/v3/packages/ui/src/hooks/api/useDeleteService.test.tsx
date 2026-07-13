import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { useDeleteService } from './useDeleteService';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('useDeleteService', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('sends a DELETE request with the params as a query string and resolves on SUCCESS', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'SUCCESS' }),
    });

    const { result } = renderHook(() => useDeleteService(), { wrapper: createWrapper() });

    const data = await result.current.mutateAsync({ serviceName: 'my-service' } as never);

    expect(data).toEqual({ result: 'SUCCESS' });
    const [calledUrl, init] = (global.fetch as jest.Mock).mock.calls[0];
    expect(calledUrl).toContain(END_POINTS.SERVICE);
    expect(calledUrl).toContain('serviceName=my-service');
    expect(init).toEqual(expect.objectContaining({ method: 'DELETE' }));
  });

  test('rejects when the HTTP response is not ok', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      json: async () => ({ result: 'FAIL', message: 'nope' }),
    });

    const { result } = renderHook(() => useDeleteService(), { wrapper: createWrapper() });

    await expect(
      result.current.mutateAsync({ serviceName: 'my-service' } as never),
    ).rejects.toEqual({ result: 'FAIL', message: 'nope' });
    await waitFor(() => expect(result.current.isError).toBe(true));
  });

  test('rejects when the payload result is not SUCCESS even if the response is ok', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'FAIL' }),
    });

    const { result } = renderHook(() => useDeleteService(), { wrapper: createWrapper() });

    await expect(
      result.current.mutateAsync({ serviceName: 'my-service' } as never),
    ).rejects.toEqual({ result: 'FAIL' });
  });
});
