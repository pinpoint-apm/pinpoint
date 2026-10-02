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
import { useAlarmV2RuleMutation } from './useAlarmV2RuleMutation';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

const BASE = END_POINTS.ALARM_V2_RULE;

const renderMutation = () =>
  renderHook(() => useAlarmV2RuleMutation(), { wrapper: createWrapper() }).result;

describe('useAlarmV2RuleMutation', () => {
  beforeEach(() => {
    global.fetch = jest.fn().mockResolvedValue({ ok: true, json: async () => ({ id: 1 }) });
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  // Same-named applications of different types are told apart by the type, so a write
  // that leaves it out is rejected.
  it.each([
    ['DELETE', { method: 'DELETE', id: 5, applicationName: 'app', applicationType: 'java' }],
    [
      'PATCH',
      { method: 'PATCH', id: 5, applicationName: 'app', applicationType: 'java', enabled: true },
    ],
  ] as const)('%s names the application and its type', async (httpMethod, variable) => {
    const result = renderMutation();

    await result.current.mutateAsync(variable as never);

    const [calledUrl, init] = (global.fetch as jest.Mock).mock.calls[0];
    expect(calledUrl).toBe(`${BASE}/5?applicationName=app&applicationType=java`);
    expect(init.method).toBe(httpMethod);
  });
});
