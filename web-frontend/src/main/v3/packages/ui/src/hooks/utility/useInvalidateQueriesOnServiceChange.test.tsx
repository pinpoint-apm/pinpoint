import React from 'react';
import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { DEFAULT_SERVICE, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { useInvalidateQueriesOnServiceChange } from './useInvalidateQueriesOnServiceChange';

const store = getDefaultStore();

const renderWithClient = (enabled: boolean) => {
  const client = new QueryClient();
  const invalidateSpy = jest.spyOn(client, 'invalidateQueries').mockResolvedValue(undefined);
  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={client}>{children}</QueryClientProvider>
  );
  const utils = renderHook(
    ({ enableServiceMap }) => useInvalidateQueriesOnServiceChange(enableServiceMap),
    { initialProps: { enableServiceMap: enabled }, wrapper },
  );
  return { ...utils, invalidateSpy };
};

beforeEach(() => {
  act(() => {
    store.set(selectedServiceAtom, DEFAULT_SERVICE);
  });
});

describe('useInvalidateQueriesOnServiceChange', () => {
  test('does not invalidate on initial mount', () => {
    const { invalidateSpy } = renderWithClient(true);
    expect(invalidateSpy).not.toHaveBeenCalled();
  });

  test('invalidates queries when the selected service changes and serviceMap is enabled', () => {
    const { invalidateSpy } = renderWithClient(true);

    act(() => {
      store.set(selectedServiceAtom, 'svc-a');
    });

    expect(invalidateSpy).toHaveBeenCalledTimes(1);
  });

  test('does not invalidate when serviceMap is disabled', () => {
    const { invalidateSpy } = renderWithClient(false);

    act(() => {
      store.set(selectedServiceAtom, 'svc-a');
    });

    expect(invalidateSpy).not.toHaveBeenCalled();
  });

  test('invalidates again on each subsequent service change', () => {
    const { invalidateSpy } = renderWithClient(true);

    act(() => {
      store.set(selectedServiceAtom, 'svc-a');
    });
    act(() => {
      store.set(selectedServiceAtom, 'svc-b');
    });

    expect(invalidateSpy).toHaveBeenCalledTimes(2);
  });

  test('does not invalidate when the service value is set but unchanged', () => {
    const { invalidateSpy } = renderWithClient(true);

    act(() => {
      store.set(selectedServiceAtom, DEFAULT_SERVICE);
    });

    expect(invalidateSpy).not.toHaveBeenCalled();
  });
});
