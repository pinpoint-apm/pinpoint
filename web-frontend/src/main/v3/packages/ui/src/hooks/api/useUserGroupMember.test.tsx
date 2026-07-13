import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';

jest.mock('./reactQueryHelper', () => ({
  queryFn: (url: string) => async () => {
    const response = await fetch(url);
    return response.json();
  },
  parseResponseError: jest.fn(async () => {
    throw new Error('parsed error');
  }),
}));

import { usePostUserGroupMember, useDeleteUserGroupMember } from './useUserGroupMember';

const createWrapper = () => {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
};

describe('useUserGroupMember mutations', () => {
  beforeEach(() => {
    global.fetch = jest.fn();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('usePostUserGroupMember POSTs and invokes onCompleteSubmit', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: true, json: async () => ({}) });
    const onCompleteSubmit = jest.fn();
    const onError = jest.fn();

    const { result } = renderHook(() => usePostUserGroupMember({ onCompleteSubmit, onError }), {
      wrapper: createWrapper(),
    });

    await result.current.onSubmit({ userGroupId: 'g1', memberId: 'm1' } as never);

    expect(global.fetch).toHaveBeenCalledWith(
      END_POINTS.CONFIG_GROUP_MEMBER,
      expect.objectContaining({ method: 'POST' }),
    );
    expect(onCompleteSubmit).toHaveBeenCalled();
    expect(onError).not.toHaveBeenCalled();
  });

  test('usePostUserGroupMember invokes onError when the request fails', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: false, json: async () => ({}) });
    const onCompleteSubmit = jest.fn();
    const onError = jest.fn();

    const { result } = renderHook(() => usePostUserGroupMember({ onCompleteSubmit, onError }), {
      wrapper: createWrapper(),
    });

    await result.current.onSubmit({ userGroupId: 'g1', memberId: 'm1' } as never);

    await waitFor(() => expect(onError).toHaveBeenCalled());
    expect(onCompleteSubmit).not.toHaveBeenCalled();
  });

  test('useDeleteUserGroupMember DELETEs and invokes onCompleteRemove', async () => {
    (global.fetch as jest.Mock).mockResolvedValueOnce({ ok: true, json: async () => ({}) });
    const onCompleteRemove = jest.fn();

    const { result } = renderHook(() => useDeleteUserGroupMember({ onCompleteRemove }), {
      wrapper: createWrapper(),
    });

    await result.current.onRemove({ userGroupId: 'g1', memberId: 'm1' } as never);

    expect(global.fetch).toHaveBeenCalledWith(
      END_POINTS.CONFIG_GROUP_MEMBER,
      expect.objectContaining({ method: 'DELETE' }),
    );
    expect(onCompleteRemove).toHaveBeenCalled();
  });
});
