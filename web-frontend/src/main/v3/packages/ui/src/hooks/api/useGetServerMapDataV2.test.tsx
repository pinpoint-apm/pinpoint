import React from 'react';
import { renderHook, waitFor, act } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

// Control the URL/atom-derived search parameters directly.
jest.mock('../searchParameters', () => ({
  useServerMapSearchParameters: jest.fn(),
}));

// reactQueryHelper transitively imports the ECharts (ESM) stack via ErrorToast,
// which babel-jest does not transform. Mock queryFn with an equivalent fetch impl.
jest.mock('./reactQueryHelper', () => ({
  queryFn: (url: string) => async () => {
    const response = await fetch(url);
    return response.json();
  },
}));

import { useServerMapSearchParameters } from '../searchParameters';
import { useGetServerMapDataV2 } from './useGetServerMapDataV2';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

const baseSearchParams = {
  dateRange: { from: new Date('2023-11-10T14:30:00Z'), to: new Date('2023-11-10T15:00:00Z') },
  search: '',
  application: { applicationName: 'TestApp', serviceType: 'SPRING_BOOT' },
  queryOption: { inbound: 1, outbound: 1, wasOnly: false, bidirectional: false },
};

describe('useGetServerMapDataV2', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
    (useServerMapSearchParameters as jest.Mock).mockReturnValue(baseSearchParams);
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('fetches the server map with the application and time range in the query', async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: true,
      json: async () => ({ applicationMapData: {} }),
    });

    const { result } = renderHook(
      () => useGetServerMapDataV2({ shouldPoll: false, useStatisticsAgentState: true }),
      { wrapper: createWrapper() },
    );

    await waitFor(() => expect(global.fetch).toHaveBeenCalled());

    const calledUrl = (global.fetch as jest.Mock).mock.calls[0][0] as string;
    expect(calledUrl).toContain(END_POINTS.SERVER_MAP_DATA_V2);
    expect(calledUrl).toContain('applicationName=TestApp');
    expect(calledUrl).toContain('serviceTypeName=SPRING_BOOT');
    await waitFor(() => expect(result.current.data).toEqual({ applicationMapData: {} }));
  });

  test('does not fetch when useStatisticsAgentState is undefined', async () => {
    const { result } = renderHook(() => useGetServerMapDataV2({ shouldPoll: false }), {
      wrapper: createWrapper(),
    });

    await act(async () => {
      await Promise.resolve();
    });

    expect(global.fetch).not.toHaveBeenCalled();
    expect(result.current.data).toBeUndefined();
  });

  test('does not fetch when no application is selected', async () => {
    (useServerMapSearchParameters as jest.Mock).mockReturnValue({
      ...baseSearchParams,
      application: null,
    });

    renderHook(() => useGetServerMapDataV2({ shouldPoll: false, useStatisticsAgentState: true }), {
      wrapper: createWrapper(),
    });

    await act(async () => {
      await Promise.resolve();
    });

    expect(global.fetch).not.toHaveBeenCalled();
  });
});
