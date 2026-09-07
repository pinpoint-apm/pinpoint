import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { configurationAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { useHiddenMapPageRedirect } from './useHiddenMapPageRedirect';

const mockLocation = { pathname: '/serverMap', search: '' };
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useLocation: () => mockLocation,
}));

const store = getDefaultStore();

const APP = 'TestApp@SPRING_BOOT';
const PERIOD = 'from=2023-11-10-14-30-00&to=2023-11-10-15-00-00';

const configWithServiceMap = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

const renderRedirect = (url: string, enableServiceMap: boolean) => {
  const [pathname, search] = url.split('?');
  mockLocation.pathname = pathname;
  mockLocation.search = search ? `?${search}` : '';
  act(() => {
    store.set(configurationAtom, configWithServiceMap(enableServiceMap));
  });
  return renderHook(() => useHiddenMapPageRedirect()).result.current;
};

describe('useHiddenMapPageRedirect', () => {
  beforeEach(() => {
    window.localStorage.clear();
    act(() => {
      store.set(selectedServiceAtom, 'DEFAULT');
      store.set(configurationAtom, undefined);
    });
  });

  afterEach(() => {
    window.localStorage.clear();
  });

  // 다른 탭에서 켠 경우. 보고 있던 servermap 화면을 servicemap으로 옮긴다.
  describe('serviceMap turned on', () => {
    test('moves the servermap page, keeping the application and the period', () => {
      expect(renderRedirect(`/serverMap/${APP}?${PERIOD}`, true)).toBe(
        `/serviceMap/DEFAULT/${APP}?${PERIOD}`,
      );
    });

    test('moves the servermap realtime page', () => {
      expect(renderRedirect(`/serverMap/realtime/${APP}`, true)).toBe(
        `/serviceMap/realtime/DEFAULT/${APP}`,
      );
    });

    // 전역 선택값이 DEFAULT가 아니면 기준 application이 없다.
    test('drops the application for a non-DEFAULT service', () => {
      act(() => {
        store.set(selectedServiceAtom, 'blogService');
      });

      expect(renderRedirect(`/serverMap/${APP}?${PERIOD}`, true)).toBe(
        `/serviceMap/blogService?${PERIOD}`,
      );
    });

    test('leaves the servicemap page alone', () => {
      expect(renderRedirect(`/serviceMap/DEFAULT/${APP}?${PERIOD}`, true)).toBeUndefined();
    });
  });

  // 다른 탭에서 끈 경우. 보고 있던 servicemap 화면을 servermap으로 옮긴다.
  describe('serviceMap turned off', () => {
    test('moves the servicemap page, dropping the service segment', () => {
      expect(renderRedirect(`/serviceMap/DEFAULT/${APP}?${PERIOD}`, false)).toBe(
        `/serverMap/${APP}?${PERIOD}`,
      );
    });

    test('moves the servicemap realtime page', () => {
      expect(renderRedirect(`/serviceMap/realtime/DEFAULT/${APP}`, false)).toBe(
        `/serverMap/realtime/${APP}`,
      );
    });

    test('leaves the servermap page alone', () => {
      expect(renderRedirect(`/serverMap/${APP}?${PERIOD}`, false)).toBeUndefined();
    });
  });

  // 사용자가 Experimental 설정에서 고른 값이 configuration 기본값을 덮는다. 화면·헤더·메뉴가
  // 모두 `pickEnableServiceMap` 하나를 지나므로 여기도 같은 값을 봐야 한다.
  test('follows the stored value over the configured default', () => {
    window.localStorage.setItem(EXPERIMENTAL_CONFIG_KEYS.ENABLE_SERVICE_MAP, 'false');

    expect(renderRedirect(`/serviceMap/DEFAULT/${APP}`, true)).toBe(`/serverMap/${APP}`);
  });

  // map 화면이 아니면 설정이 어느 쪽이든 옮기지 않는다. filteredMap은 두 모드에 모두 남는다.
  test('never moves other pages', () => {
    expect(renderRedirect(`/filteredMap/DEFAULT/${APP}`, true)).toBeUndefined();
    expect(renderRedirect(`/filteredMap/${APP}`, false)).toBeUndefined();
    expect(renderRedirect('/config/experimental', true)).toBeUndefined();
    expect(renderRedirect('/config/experimental', false)).toBeUndefined();
    expect(renderRedirect(`/inspector/${APP}`, true)).toBeUndefined();
  });
});
