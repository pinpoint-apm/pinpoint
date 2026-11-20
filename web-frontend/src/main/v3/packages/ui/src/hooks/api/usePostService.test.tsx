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

    await expect(result.current.mutateAsync({ serviceName: 'x' } as never)).rejects.toMatchObject({
      message: 'boom',
    });
  });

  test('rejects when the payload result is not SUCCESS even if the response is ok', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'FAIL' }),
    });

    const { result } = renderHook(() => usePostService(), { wrapper: createWrapper() });

    await expect(result.current.mutateAsync({ serviceName: 'x' } as never)).rejects.toMatchObject({
      result: 'FAIL',
    });
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

    const { result } = renderHook(() => usePostService(), { wrapper: createWrapper() });

    const error = await result.current
      .mutateAsync({ serviceName: 'x' } as never)
      .catch((e: unknown) => e);
    expect(error).toBeInstanceOf(Error);
    expect(error).toMatchObject({ status: 502 });
  });

  test('rejects with an Error using the server message when the result is not SUCCESS', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'FAIL', message: 'already exists' }),
    });

    const { result } = renderHook(() => usePostService(), { wrapper: createWrapper() });

    const error = await result.current
      .mutateAsync({ serviceName: 'x' } as never)
      .catch((e: unknown) => e);
    expect(error).toBeInstanceOf(Error);
    expect(error).toMatchObject({ result: 'FAIL', message: 'already exists' });
  });
});
