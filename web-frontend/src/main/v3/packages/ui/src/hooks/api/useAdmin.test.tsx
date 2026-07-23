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

  test('useDeleteApplication DELETEs the applications endpoint with name, serviceType and password', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: true });

    const { result } = renderHook(() => useDeleteApplication(), { wrapper: createWrapper() });
    const data = await result.current.mutateAsync({
      applicationName: 'App',
      serviceTypeName: 'SPRING_BOOT',
      password: 'pw',
    });

    expect(data).toBeNull();
    const [calledUrl, calledInit] = (global.fetch as jest.Mock).mock.calls[0];
    expect(calledUrl as string).toContain(END_POINTS.ADMIN_APPLICATIONS);
    expect(calledUrl as string).toContain('applicationName=App');
    expect(calledUrl as string).toContain('serviceTypeName=SPRING_BOOT');
    expect(calledUrl as string).toContain('password=pw');
    expect((calledInit as RequestInit).method).toBe('DELETE');
  });

  test('useDeleteAgent DELETEs the agents endpoint with agentId and serviceType', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: true });

    const { result } = renderHook(() => useDeleteAgent(), { wrapper: createWrapper() });
    await result.current.mutateAsync({
      applicationName: 'App',
      serviceTypeName: 'SPRING_BOOT',
      agentId: 'a1',
      password: 'pw',
    });

    const [calledUrl, calledInit] = (global.fetch as jest.Mock).mock.calls[0];
    expect(calledUrl as string).toContain(END_POINTS.ADMIN_AGENTS);
    expect(calledUrl as string).toContain('agentId=a1');
    expect(calledUrl as string).toContain('serviceTypeName=SPRING_BOOT');
    expect((calledInit as RequestInit).method).toBe('DELETE');
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
