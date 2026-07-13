import React from 'react';
import { renderHook, waitFor, act } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

jest.mock('../searchParameters', () => ({
  useFilteredMapParameters: jest.fn(),
}));

// reactQueryHelper transitively imports the ECharts (ESM) stack via ErrorToast,
// which babel-jest does not transform. Mock queryFn with an equivalent fetch impl.
jest.mock('./reactQueryHelper', () => ({
  queryFn: (url: string) => async () => {
    const response = await fetch(url);
    return response.json();
  },
}));

import { useFilteredMapParameters } from '../searchParameters';
import { useGetFilteredServerMapData } from './useGetFilteredServerMapData';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

const baseParams = {
  search: '',
  dateRange: { from: new Date('2023-11-10T14:30:00Z'), to: new Date('2023-11-10T15:00:00Z') },
  searchParameters: { filter: '[{"a":"b"}]', hint: '' },
  application: { applicationName: 'TestApp', serviceType: 'SPRING_BOOT' },
  parsedFilters: [],
  parsedHint: {},
};

describe('useGetFilteredServerMapData', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
    (useFilteredMapParameters as jest.Mock).mockReturnValue(baseParams);
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('fetches the filtered server map with application and filter in the query', async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: true,
      json: async () => ({ applicationMapData: {} }),
    });

    const { result } = renderHook(() => useGetFilteredServerMapData(false), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(global.fetch).toHaveBeenCalled());

    const calledUrl = (global.fetch as jest.Mock).mock.calls[0][0] as string;
    expect(calledUrl).toContain(END_POINTS.FILTERED_SERVER_MAP_DATA);
    expect(calledUrl).toContain('applicationName=TestApp');
    expect(calledUrl).toContain('serviceTypeName=SPRING_BOOT');
    await waitFor(() => expect(result.current.data).toEqual({ applicationMapData: {} }));
  });

  test('does not fetch while paused', async () => {
    renderHook(() => useGetFilteredServerMapData(true), { wrapper: createWrapper() });

    await act(async () => {
      await Promise.resolve();
    });

    expect(global.fetch).not.toHaveBeenCalled();
  });

  test('does not fetch when the filter is missing', async () => {
    (useFilteredMapParameters as jest.Mock).mockReturnValue({
      ...baseParams,
      searchParameters: { filter: undefined, hint: '' },
    });

    renderHook(() => useGetFilteredServerMapData(false), { wrapper: createWrapper() });

    await act(async () => {
      await Promise.resolve();
    });

    expect(global.fetch).not.toHaveBeenCalled();
  });
});
