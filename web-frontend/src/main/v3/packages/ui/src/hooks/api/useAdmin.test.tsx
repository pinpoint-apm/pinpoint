import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

jest.mock('./reactQueryHelper', () => ({
  parseResponseError: jest.fn(async () => {
    throw new Error('parsed error');
  }),
}));

import { useDeleteApplication, useDeleteAgent } from './useAdmin';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('useAdmin', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('useDeleteApplication calls the remove-application endpoint with name and password', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: true });

    const { result } = renderHook(() => useDeleteApplication(), { wrapper: createWrapper() });
    const data = await result.current.mutateAsync({ applicationName: 'App', password: 'pw' });

    expect(data).toBeNull();
    const calledUrl = (global.fetch as jest.Mock).mock.calls[0][0] as string;
    expect(calledUrl).toContain(END_POINTS.ADMIN_REMOVE_APPLICATION);
    expect(calledUrl).toContain('applicationName=App');
    expect(calledUrl).toContain('password=pw');
  });

  test('useDeleteAgent calls the remove-agent endpoint with agentId', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: true });

    const { result } = renderHook(() => useDeleteAgent(), { wrapper: createWrapper() });
    await result.current.mutateAsync({ applicationName: 'App', agentId: 'a1', password: 'pw' });

    const calledUrl = (global.fetch as jest.Mock).mock.calls[0][0] as string;
    expect(calledUrl).toContain(END_POINTS.ADMIN_REMOVE_AGENT);
    expect(calledUrl).toContain('agentId=a1');
  });

  test('rejects through parseResponseError when the response is not ok', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: false, json: async () => ({}) });

    const { result } = renderHook(() => useDeleteApplication(), { wrapper: createWrapper() });

    await expect(
      result.current.mutateAsync({ applicationName: 'App', password: 'pw' }),
    ).rejects.toThrow('parsed error');
    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
