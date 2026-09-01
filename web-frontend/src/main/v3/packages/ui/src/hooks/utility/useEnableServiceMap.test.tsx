import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { useEnableServiceMap } from './useEnableServiceMap';
import { useExperimentals } from './useExperimentals';

const store = getDefaultStore();

const configWithServiceMap = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

const renderEnableServiceMap = (configuration?: Configuration) => {
  act(() => {
    store.set(configurationAtom, configuration);
  });
  return renderHook(() => useEnableServiceMap());
};

describe('useEnableServiceMap', () => {
  beforeEach(() => {
    window.localStorage.clear();
    act(() => {
      store.set(configurationAtom, undefined);
    });
  });

  test('uses the configuration value as the default', () => {
    expect(renderEnableServiceMap(configWithServiceMap(true)).result.current).toBe(true);
    expect(renderEnableServiceMap(configWithServiceMap(false)).result.current).toBe(false);
  });

  test('the stored value wins over the configuration default', () => {
    window.localStorage.setItem(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP, 'true');

    expect(renderEnableServiceMap(configWithServiceMap(false)).result.current).toBe(true);
  });

  // 값을 읽기만 해도 localStorage에 기록되면, 사용자가 고른 적 없는 값이 서버 기본값을 덮어쓴다.
  // (configuration이 도착하기 전에 마운트되면 false가 박힌다.)
  test('reading does not write anything to localStorage', () => {
    renderEnableServiceMap(undefined);
    renderEnableServiceMap(configWithServiceMap(true));

    expect(window.localStorage.getItem(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP)).toBeNull();
  });

  test('a configuration that arrives after mount is picked up', () => {
    const { result } = renderEnableServiceMap(undefined);
    expect(result.current).toBe(false);

    act(() => {
      store.set(configurationAtom, configWithServiceMap(true));
    });

    expect(result.current).toBe(true);
  });

  test('toggling it in the Experimental page reaches readers without a reload', () => {
    act(() => {
      store.set(configurationAtom, configWithServiceMap(false));
    });
    const reader = renderHook(() => useEnableServiceMap());
    const experimentals = renderHook(() => useExperimentals(configWithServiceMap(false)));

    expect(reader.result.current).toBe(false);
    expect(experimentals.result.current[EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP].value).toBe(
      false,
    );

    act(() => {
      experimentals.result.current[EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP].setter(true);
    });

    expect(reader.result.current).toBe(true);
    // 렌더 밖(fetch 인터셉터)도 같은 값을 읽어야 하므로 localStorage에 남아야 한다.
    expect(window.localStorage.getItem(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP)).toBe('true');
  });
});
