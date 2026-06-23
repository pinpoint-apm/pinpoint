import { renderHook } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { servicesAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

// useGetServices는 React Query/네트워크에 의존하므로 barrel을 모킹해 격리한다.
jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  useGetServices: jest.fn(),
}));

import { useGetServices } from '@pinpoint-fe/ui/src/hooks';
import { useServicesFetch } from './useServicesFetch';

const mockedUseGetServices = useGetServices as unknown as jest.Mock;
const store = getDefaultStore();

const configWith = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

beforeEach(() => {
  mockedUseGetServices.mockReset();
  store.set(servicesAtom, undefined);
});

describe('useServicesFetch', () => {
  test('keeps servicesAtom undefined and disables the query when enableServiceMap is off', () => {
    mockedUseGetServices.mockReturnValue({ data: ['DEFAULT', 'a'] });

    renderHook(() => useServicesFetch(configWith(false)));

    expect(mockedUseGetServices).toHaveBeenCalledWith({ enabled: false });
    expect(store.get(servicesAtom)).toBeUndefined();
  });

  test('syncs servicesAtom from the query and enables it when enableServiceMap is on', () => {
    mockedUseGetServices.mockReturnValue({ data: ['DEFAULT', 'a', 'b'] });

    renderHook(() => useServicesFetch(configWith(true)));

    expect(mockedUseGetServices).toHaveBeenCalledWith({ enabled: true });
    expect(store.get(servicesAtom)).toEqual(['DEFAULT', 'a', 'b']);
  });

  test('keeps servicesAtom undefined when configuration is undefined', () => {
    mockedUseGetServices.mockReturnValue({ data: undefined });

    renderHook(() => useServicesFetch(undefined));

    expect(mockedUseGetServices).toHaveBeenCalledWith({ enabled: false });
    expect(store.get(servicesAtom)).toBeUndefined();
  });

  test('resets servicesAtom to undefined when enableServiceMap is turned off', () => {
    mockedUseGetServices.mockReturnValue({ data: ['DEFAULT', 'a'] });

    const { rerender } = renderHook(({ config }) => useServicesFetch(config), {
      initialProps: { config: configWith(true) },
    });
    expect(store.get(servicesAtom)).toEqual(['DEFAULT', 'a']);

    rerender({ config: configWith(false) });
    expect(store.get(servicesAtom)).toBeUndefined();
  });
});
