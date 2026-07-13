import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

// reactQueryHelper transitively imports the ECharts (ESM) stack via ErrorToast.
// Mock queryFn/parseResponseError so babel-jest does not choke and we can drive errors.
jest.mock('./reactQueryHelper', () => ({
  queryFn: (url: string) => async () => {
    const response = await fetch(url);
    return response.json();
  },
  parseResponseError: jest.fn(async () => {
    throw new Error('parsed error');
  }),
}));

import { usePostConfigUser, usePutConfigUser, useDeleteConfigUser } from './useConfigUsers';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('useConfigUsers mutations', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('usePostConfigUser POSTs the user payload', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: true, json: async () => ({}) });
    const { result } = renderHook(() => usePostConfigUser(), { wrapper: createWrapper() });

    const user = { userId: 'u1', name: 'User' } as never;
    await result.current.mutateAsync(user);

    expect(global.fetch).toHaveBeenCalledWith(
      END_POINTS.CONFIG_USERS,
      expect.objectContaining({ method: 'POST', body: JSON.stringify(user) }),
    );
  });

  test('usePutConfigUser PUTs the user payload', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: true, json: async () => ({}) });
    const { result } = renderHook(() => usePutConfigUser(), { wrapper: createWrapper() });

    await result.current.mutateAsync({ userId: 'u1' } as never);

    expect((global.fetch as jest.Mock).mock.calls[0][1]).toEqual(
      expect.objectContaining({ method: 'PUT' }),
    );
  });

  test('useDeleteConfigUser DELETEs with the userId in the body', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: true, json: async () => ({}) });
    const { result } = renderHook(() => useDeleteConfigUser(), { wrapper: createWrapper() });

    await result.current.mutateAsync('u1');

    expect(global.fetch).toHaveBeenCalledWith(
      END_POINTS.CONFIG_USERS,
      expect.objectContaining({ method: 'DELETE', body: JSON.stringify({ userId: 'u1' }) }),
    );
  });

  test('rejects through parseResponseError when the response is not ok', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: false, json: async () => ({}) });
    const { result } = renderHook(() => usePostConfigUser(), { wrapper: createWrapper() });

    await expect(result.current.mutateAsync({} as never)).rejects.toThrow('parsed error');
    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
