import React from 'react';
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { usePostBind } from './usePostBind';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('usePostBind', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('POSTs the FormData to the bind endpoint and returns the parsed response', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'ok' }),
    });

    const { result } = renderHook(() => usePostBind(), { wrapper: createWrapper() });

    const formData = new FormData();
    formData.append('file', 'value');
    const data = await result.current.mutateAsync(formData);

    expect(data).toEqual({ result: 'ok' });
    expect(global.fetch).toHaveBeenCalledWith(
      END_POINTS.BIND,
      expect.objectContaining({ method: 'POST', body: formData }),
    );
  });

  test('rejects when the fetch itself fails', async () => {
    (global.fetch as jest.Mock).mockRejectedValueOnce(new Error('network down'));

    const { result } = renderHook(() => usePostBind(), { wrapper: createWrapper() });

    await expect(result.current.mutateAsync(new FormData())).rejects.toThrow('network down');
  });
});
