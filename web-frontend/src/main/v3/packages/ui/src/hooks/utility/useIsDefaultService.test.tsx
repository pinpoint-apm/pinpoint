import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { configurationAtom, DEFAULT_SERVICE, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';
import { useIsDefaultService } from './useIsDefaultService';

const mockLocation = { pathname: '/serviceMap' };
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useLocation: () => mockLocation,
}));

const store = getDefaultStore();

const configWithServiceMap = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

const renderIsDefaultService = ({
  pathname = '/serviceMap',
  service,
  enableServiceMap = true,
}: {
  pathname?: string;
  service: string;
  enableServiceMap?: boolean;
}) => {
  mockLocation.pathname = pathname;
  act(() => {
    store.set(selectedServiceAtom, service);
    store.set(configurationAtom, configWithServiceMap(enableServiceMap));
  });
  return renderHook(() => useIsDefaultService()).result.current;
};

describe('useIsDefaultService', () => {
  test('is true for the DEFAULT service, which draws one picked application', () => {
    expect(renderIsDefaultService({ service: DEFAULT_SERVICE })).toBe(true);
  });

  test('is false for another service, which draws every application it owns', () => {
    expect(renderIsDefaultService({ service: 'blogService' })).toBe(false);
  });

  // 설정이 꺼져 있으면 백엔드가 모든 요청을 기본 service로 해석한다.
  test('is true when enableServiceMap is off, whatever is stored as the selection', () => {
    expect(renderIsDefaultService({ service: 'blogService', enableServiceMap: false })).toBe(true);
  });

  test('prefers the service carried in the path over the globally selected one', () => {
    expect(
      renderIsDefaultService({
        pathname: `/transactionList/${DEFAULT_SERVICE}/test-app@SPRING_BOOT`,
        service: 'blogService',
      }),
    ).toBe(true);
  });
});
