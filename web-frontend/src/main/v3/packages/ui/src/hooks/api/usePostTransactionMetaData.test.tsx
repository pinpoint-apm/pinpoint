// The hook reaches reactQueryHelper for parseResponseError, and that pulls ErrorToast ->
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
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { usePostTransactionMetaData } from './usePostTransactionMetaData';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('usePostTransactionMetaData', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('serializes the FormData as url-encoded body and POSTs it', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ metadata: [] }),
    });

    const { result } = renderHook(() => usePostTransactionMetaData(), {
      wrapper: createWrapper(),
    });

    const formData = new FormData();
    formData.append('traceId', 't1');
    formData.append('spanId', 's1');
    const data = await result.current.mutateAsync(formData);

    expect(data).toEqual({ metadata: [] });
    const [url, init] = (global.fetch as jest.Mock).mock.calls[0];
    expect(url).toBe(END_POINTS.TRANSACTION_META_DATA);
    expect(init.method).toBe('POST');
    expect(init.headers).toEqual({ 'Content-Type': 'application/x-www-form-urlencoded' });
    expect(init.body).toContain('traceId=t1');
    expect(init.body).toContain('spanId=s1');
  });

  test('rejects when the fetch itself fails', async () => {
    (global.fetch as jest.Mock).mockRejectedValueOnce(new Error('network down'));

    const { result } = renderHook(() => usePostTransactionMetaData(), {
      wrapper: createWrapper(),
    });

    await expect(result.current.mutateAsync(new FormData())).rejects.toThrow('network down');
  });

  test('rejects with the status code when the server answers with an error', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 400,
      json: async () => ({ status: 400, title: 'Bad Request', detail: 'invalid request' }),
    });

    const { result } = renderHook(() => usePostTransactionMetaData(), {
      wrapper: createWrapper(),
    });

    await expect(result.current.mutateAsync(new FormData())).rejects.toMatchObject({
      status: 400,
      message: 'invalid request',
    });
  });

  test('keeps the status code when the error body is not JSON', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({
      ok: false,
      status: 502,
      json: async () => {
        throw new SyntaxError('Unexpected token <');
      },
    });

    const { result } = renderHook(() => usePostTransactionMetaData(), {
      wrapper: createWrapper(),
    });

    await expect(result.current.mutateAsync(new FormData())).rejects.toMatchObject({ status: 502 });
  });
});
