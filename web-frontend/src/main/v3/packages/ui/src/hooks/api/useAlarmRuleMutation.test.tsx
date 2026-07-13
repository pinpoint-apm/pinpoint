import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { useAlarmRuleMutation } from './useAlarmRuleMutation';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('useAlarmRuleMutation', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('sends the request with the given method and body to the alarm rule endpoint', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'SUCCESS' }),
    });

    const { result } = renderHook(() => useAlarmRuleMutation(), { wrapper: createWrapper() });

    const params = { ruleId: 'r1' } as never;
    const data = await result.current.mutateAsync({ params, method: 'POST' });

    expect(data).toEqual({ result: 'SUCCESS' });
    expect(global.fetch).toHaveBeenCalledWith(
      END_POINTS.ALARM_RULE,
      expect.objectContaining({
        method: 'POST',
        body: JSON.stringify(params),
        headers: { 'Content-Type': 'application/json' },
      }),
    );
  });

  test('passes DELETE through as the request method', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'SUCCESS' }),
    });

    const { result } = renderHook(() => useAlarmRuleMutation(), { wrapper: createWrapper() });
    await result.current.mutateAsync({ params: { ruleId: 'r1' } as never, method: 'DELETE' });

    expect((global.fetch as jest.Mock).mock.calls[0][1]).toEqual(
      expect.objectContaining({ method: 'DELETE' }),
    );
  });

  test('rejects when the payload result is not SUCCESS', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'FAIL', message: 'bad' }),
    });

    const { result } = renderHook(() => useAlarmRuleMutation(), { wrapper: createWrapper() });

    await expect(
      result.current.mutateAsync({ params: {} as never, method: 'POST' }),
    ).rejects.toEqual({ result: 'FAIL', message: 'bad' });
    await waitFor(() => expect(result.current.isError).toBe(true));
  });
});
