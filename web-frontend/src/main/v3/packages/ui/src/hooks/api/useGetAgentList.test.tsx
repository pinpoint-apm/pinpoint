import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

// reactQueryHelper transitively imports the ECharts (ESM) stack via ErrorToast,
// which babel-jest does not transform. Mock queryFn with an equivalent fetch impl.
jest.mock('./reactQueryHelper', () => ({
  queryFn: (url: string) => async () => {
    const response = await fetch(url);
    return response.json();
  },
}));

import { useGetAgentList } from './useGetAgentList';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>
      <React.Suspense fallback={null}>{children}</React.Suspense>
    </QueryClientProvider>
  );
};

const params = {
  applicationName: 'TestApp',
  serviceTypeName: 'SPRING_BOOT',
  from: 1699626600000,
  to: 1699628400000,
};

describe('useGetAgentList', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  test('fetches the agent list with the application name in the query', async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: true,
      json: async () => [{ agentId: 'agent-1' }],
    });

    renderHook(() => useGetAgentList(params), { wrapper: createWrapper() });

    await waitFor(() => expect(global.fetch).toHaveBeenCalled());
    const calledUrl = (global.fetch as jest.Mock).mock.calls[0][0] as string;
    expect(calledUrl).toContain(END_POINTS.AGENT_LIST);
    expect(calledUrl).toContain('applicationName=TestApp');
    expect(calledUrl).toContain('serviceTypeName=SPRING_BOOT');
  });

  test('returns the agents sorted by agentId', async () => {
    (global.fetch as jest.Mock).mockResolvedValue({
      ok: true,
      json: async () => [{ agentId: 'zeta' }, { agentId: 'alpha' }, { agentId: 'mike' }],
    });

    const { result } = renderHook(() => useGetAgentList(params), { wrapper: createWrapper() });

    await waitFor(() => expect(result.current.data.length).toBe(3));
    expect(result.current.data.map((a) => a.agentId)).toEqual(['alpha', 'mike', 'zeta']);
  });

  test('does not fetch and returns an empty list when the application name is missing', async () => {
    const { result } = renderHook(() => useGetAgentList({ ...params, applicationName: '' }), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.data).toEqual([]));
    expect(global.fetch).not.toHaveBeenCalled();
  });
});
