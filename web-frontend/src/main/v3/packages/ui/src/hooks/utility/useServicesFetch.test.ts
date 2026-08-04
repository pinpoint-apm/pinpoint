import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { configurationAtom, servicesAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

// useGetServices는 React Query/네트워크에 의존하므로 barrel을 모킹해 격리한다.
// (useConfiguration은 barrel이 아닌 상대 경로로 임포트되므로 이 모킹에 영향받지 않는다.)
jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  useGetServices: jest.fn(),
}));

import { useGetServices } from '@pinpoint-fe/ui/src/hooks';
import { useServicesFetch } from './useServicesFetch';

const mockedUseGetServices = useGetServices as unknown as jest.Mock;
const store = getDefaultStore();

const configWith = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

const setConfiguration = (configuration: Configuration | undefined) =>
  act(() => {
    store.set(configurationAtom, configuration);
  });

beforeEach(() => {
  mockedUseGetServices.mockReset();
  store.set(servicesAtom, undefined);
  store.set(configurationAtom, undefined);
});

describe('useServicesFetch', () => {
  test('keeps servicesAtom undefined and disables the query when enableServiceMap is off', () => {
    mockedUseGetServices.mockReturnValue({ data: ['DEFAULT', 'a'] });
    setConfiguration(configWith(false));

    renderHook(() => useServicesFetch());

    expect(mockedUseGetServices).toHaveBeenCalledWith({ enabled: false });
    expect(store.get(servicesAtom)).toBeUndefined();
  });

  test('syncs servicesAtom from the query and enables it when enableServiceMap is on', () => {
    mockedUseGetServices.mockReturnValue({ data: ['DEFAULT', 'a', 'b'] });
    setConfiguration(configWith(true));

    renderHook(() => useServicesFetch());

    expect(mockedUseGetServices).toHaveBeenCalledWith({ enabled: true });
    expect(store.get(servicesAtom)).toEqual(['DEFAULT', 'a', 'b']);
  });

  test('keeps servicesAtom undefined when configuration is not loaded yet', () => {
    mockedUseGetServices.mockReturnValue({ data: undefined });

    renderHook(() => useServicesFetch());

    expect(mockedUseGetServices).toHaveBeenCalledWith({ enabled: false });
    expect(store.get(servicesAtom)).toBeUndefined();
  });

  test('resets servicesAtom to undefined when enableServiceMap is turned off', () => {
    mockedUseGetServices.mockReturnValue({ data: ['DEFAULT', 'a'] });
    setConfiguration(configWith(true));

    renderHook(() => useServicesFetch());
    expect(store.get(servicesAtom)).toEqual(['DEFAULT', 'a']);

    // atom을 바꾸면 훅이 구독하고 있으므로 다시 렌더되어 동기화된다.
    setConfiguration(configWith(false));
    expect(store.get(servicesAtom)).toBeUndefined();
  });
});
