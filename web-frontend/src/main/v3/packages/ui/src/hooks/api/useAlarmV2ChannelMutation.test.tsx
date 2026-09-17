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
import { useAlarmV2ChannelMutation } from './useAlarmV2ChannelMutation';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

const APP = 'test-app';
const PARAMS = { name: 'channel', methodType: 'EMAIL' } as never;
const BASE = END_POINTS.ALARM_V2_CHANNEL;

const renderMutation = () =>
  renderHook(() => useAlarmV2ChannelMutation(), { wrapper: createWrapper() }).result;

const lastCall = () => (global.fetch as jest.Mock).mock.calls[0];

describe('useAlarmV2ChannelMutation', () => {
  beforeEach(() => {
    global.fetch = jest.fn().mockResolvedValue({ ok: true, json: async () => ({ id: 1 }) });
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  // LINK and UNLINK differ only by HTTP method on the same path. Swap them and a save
  // that meant to attach a channel detaches it instead -- both return void, so the
  // caller cannot tell, and the rule goes on firing to nowhere.
  it.each([
    ['POST', { method: 'POST', applicationName: APP, params: PARAMS }, 'POST', `${BASE}?applicationName=test-app`],
    [
      'PUT',
      { method: 'PUT', id: 5, applicationName: APP, params: PARAMS },
      'PUT',
      `${BASE}/5?applicationName=test-app`,
    ],
    ['DELETE', { method: 'DELETE', id: 5, applicationName: APP }, 'DELETE', `${BASE}/5?applicationName=test-app`],
    [
      'LINK',
      { method: 'LINK', ruleId: 3, channelId: 9, applicationName: APP },
      'POST',
      `${BASE}/rule/3/9?applicationName=test-app`,
    ],
    [
      'UNLINK',
      { method: 'UNLINK', ruleId: 3, channelId: 9, applicationName: APP },
      'DELETE',
      `${BASE}/rule/3/9?applicationName=test-app`,
    ],
    [
      'LINK_TEMPLATE',
      { method: 'LINK_TEMPLATE', templateId: 4, channelId: 9, applicationName: APP },
      'POST',
      `${BASE}/template/4/9?applicationName=test-app`,
    ],
    [
      'UNLINK_TEMPLATE',
      { method: 'UNLINK_TEMPLATE', templateId: 4, channelId: 9, applicationName: APP },
      'DELETE',
      `${BASE}/template/4/9?applicationName=test-app`,
    ],
  ] as const)('%s maps to %s on the right path', async (_name, variable, httpMethod, url) => {
    const result = renderMutation();

    await result.current.mutateAsync(variable as never);

    const [calledUrl, init] = lastCall();
    expect(calledUrl).toBe(url);
    expect(init.method).toBe(httpMethod);
  });

  it('sends a JSON body only when there is one', async () => {
    const result = renderMutation();

    await result.current.mutateAsync({ method: 'POST', applicationName: APP, params: PARAMS } as never);

    const [, init] = lastCall();
    expect(init.body).toBe(JSON.stringify(PARAMS));
    expect(init.headers).toEqual({ 'Content-Type': 'application/json' });
  });

  it('omits body and content type for link requests', async () => {
    const result = renderMutation();

    await result.current.mutateAsync({
      method: 'LINK',
      ruleId: 3,
      channelId: 9,
      applicationName: APP,
    } as never);

    const [, init] = lastCall();
    expect(init.body).toBeUndefined();
    expect(init.headers).toBeUndefined();
  });

  // The application name is the permission scope the server authorizes against, so a
  // name with regex or path characters has to survive as one query parameter.
  it('encodes the application name', async () => {
    const result = renderMutation();

    await result.current.mutateAsync({
      method: 'DELETE',
      id: 5,
      applicationName: 'app/with space&x',
    } as never);

    expect(lastCall()[0]).toBe(`${BASE}/5?applicationName=app%2Fwith%20space%26x`);
  });

  it('returns the created channel for POST and nothing for the rest', async () => {
    const result = renderMutation();

    await expect(
      result.current.mutateAsync({ method: 'POST', applicationName: APP, params: PARAMS } as never),
    ).resolves.toEqual({ id: 1 });
    await expect(
      result.current.mutateAsync({ method: 'DELETE', id: 5, applicationName: APP } as never),
    ).resolves.toBeUndefined();
  });

  it('rejects when the response is not ok', async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: false,
      status: 403,
      json: async () => ({ message: 'forbidden' }),
    });
    const result = renderMutation();

    await expect(
      result.current.mutateAsync({ method: 'DELETE', id: 5, applicationName: APP } as never),
    ).rejects.toBeDefined();
  });
});
