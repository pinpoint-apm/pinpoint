// The hook reaches reactQueryHelper for requestJson, and that pulls ErrorToast ->
// the ECharts (ESM) stack babel-jest does not transform. Stubbed the same way
// reactQueryHelper.test.tsx does, so the mocks must precede the imports below.
jest.mock('../../components/Error/ErrorToast', () => ({ ErrorToast: () => null }));
jest.mock('react-toastify', () => ({ toast: { error: jest.fn() } }));
jest.mock('@pinpoint-fe/ui/src/atoms', () => {
  const { atom } = require('jotai');
  return {
    toastCountAtom: atom(0),
    selectedServiceAtom: atom('DEFAULT'),
    configurationAtom: atom(undefined),
    DEFAULT_SERVICE: 'DEFAULT',
  };
});

import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { useWebhookIncludeMutation } from './useWebhookIncludeMutation';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('useWebhookIncludeMutation', () => {
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

    const { result } = renderHook(() => useWebhookIncludeMutation(), { wrapper: createWrapper() });

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

    const { result } = renderHook(() => useWebhookIncludeMutation(), { wrapper: createWrapper() });

    await expect(
      result.current.mutateAsync({ params: {} as never, method: 'PUT' }),
    ).rejects.toMatchObject({ result: 'FAIL' });
    await waitFor(() => expect(result.current.isError).toBe(true));
  });

  test('rejects with an Error carrying the status code when the error body is not JSON', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 502,
      json: async () => {
        throw new SyntaxError('Unexpected token <');
      },
    });

    const { result } = renderHook(() => useWebhookIncludeMutation(), { wrapper: createWrapper() });

    const error = await result.current
      .mutateAsync({ params: {} as never, method: 'POST' })
      .catch((e: unknown) => e);
    expect(error).toBeInstanceOf(Error);
    expect(error).toMatchObject({ status: 502 });
  });

  test('rejects with an Error using the server message when the result is not SUCCESS', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'FAIL', message: 'already exists' }),
    });

    const { result } = renderHook(() => useWebhookIncludeMutation(), { wrapper: createWrapper() });

    const error = await result.current
      .mutateAsync({ params: {} as never, method: 'POST' })
      .catch((e: unknown) => e);
    expect(error).toBeInstanceOf(Error);
    expect(error).toMatchObject({ result: 'FAIL', message: 'already exists' });
  });
});
