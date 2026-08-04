import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { configurationAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';
import { useServiceNameForLink } from './useServiceNameForLink';

const mockLocation = { pathname: '/serviceMap/test-app@SPRING_BOOT' };
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useLocation: () => mockLocation,
}));

const store = getDefaultStore();

const configWithServiceMap = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

const renderServiceNameForLink = (pathname: string, configuration?: Configuration) => {
  mockLocation.pathname = pathname;
  act(() => {
    store.set(configurationAtom, configuration);
  });
  return renderHook(() => useServiceNameForLink()).result.current;
};

describe('useServiceNameForLink', () => {
  beforeEach(() => {
    act(() => {
      store.set(selectedServiceAtom, 'my-service');
      store.set(configurationAtom, undefined);
    });
  });

  test('returns the selected service on a service scoped page', () => {
    expect(
      renderServiceNameForLink('/serviceMap/test-app@SPRING_BOOT', configWithServiceMap(true)),
    ).toBe('my-service');
  });

  test('returns undefined when enableServiceMap is off', () => {
    expect(
      renderServiceNameForLink('/serviceMap/test-app@SPRING_BOOT', configWithServiceMap(false)),
    ).toBeUndefined();
  });

  test('returns undefined when no configuration is loaded yet', () => {
    // configuration은 부트스트랩 이후 비동기로 로드되므로 아직 없을 수 있다.
    expect(renderServiceNameForLink('/serviceMap/test-app@SPRING_BOOT')).toBeUndefined();
  });

  test('returns the selected service on the servermap page too', () => {
    // ServerMap도 예외가 아니라 선택된 service 범위에서 조회된다.
    const config = configWithServiceMap(true);
    expect(renderServiceNameForLink('/serverMap/test-app@SPRING_BOOT', config)).toBe('my-service');
    expect(renderServiceNameForLink('/serverMap/realtime/test-app@SPRING_BOOT', config)).toBe(
      'my-service',
    );
  });

  test('prefers the service name already carried by the path', () => {
    expect(
      renderServiceNameForLink(
        '/transactionList/url-service@test-app@SPRING_BOOT',
        configWithServiceMap(true),
      ),
    ).toBe('url-service');
  });

  test('falls back to the selected service on a legacy transactionList path', () => {
    expect(
      renderServiceNameForLink('/transactionList/test-app@SPRING_BOOT', configWithServiceMap(true)),
    ).toBe('my-service');
  });
});
