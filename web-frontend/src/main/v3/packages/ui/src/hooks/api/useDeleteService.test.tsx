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
    ).rejects.toMatchObject({ message: 'nope' });
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
    ).rejects.toMatchObject({ result: 'FAIL' });
  });

  test('rejects with an Error carrying the status code when the error body is not JSON', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 502,
      json: async () => {
        throw new SyntaxError('Unexpected token <');
      },
    });

    const { result } = renderHook(() => useDeleteService(), { wrapper: createWrapper() });

    const error = await result.current
      .mutateAsync({ serviceName: 'my-service' } as never)
      .catch((e: unknown) => e);
    expect(error).toBeInstanceOf(Error);
    expect(error).toMatchObject({ status: 502 });
  });

  test('rejects with an Error using the server message when the result is not SUCCESS', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result: 'FAIL', message: 'already exists' }),
    });

    const { result } = renderHook(() => useDeleteService(), { wrapper: createWrapper() });

    const error = await result.current
      .mutateAsync({ serviceName: 'my-service' } as never)
      .catch((e: unknown) => e);
    expect(error).toBeInstanceOf(Error);
    expect(error).toMatchObject({ result: 'FAIL', message: 'already exists' });
  });
});
