import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { DEFAULT_SERVICE, selectedServiceAtom, servicesAtom } from '@pinpoint-fe/ui/src/atoms';
import { useSyncSelectedServiceWithPath } from './useSyncSelectedServiceWithPath';

const mockLocation = { pathname: '/serviceMap/aaa' };
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useLocation: () => mockLocation,
}));

const store = getDefaultStore();

const render = ({
  pathname,
  selected,
  services,
  enabled = true,
}: {
  pathname: string;
  selected: string;
  services?: string[];
  enabled?: boolean;
}) => {
  mockLocation.pathname = pathname;
  act(() => {
    store.set(selectedServiceAtom, selected);
    store.set(servicesAtom, services);
  });
  return renderHook(() => useSyncSelectedServiceWithPath(enabled));
};

describe('useSyncSelectedServiceWithPath', () => {
  describe('a service that exists', () => {
    test('becomes the selected service', () => {
      const { result } = render({
        pathname: '/serviceMap/bbb',
        selected: 'aaa',
        services: [DEFAULT_SERVICE, 'aaa', 'bbb'],
      });

      expect(store.get(selectedServiceAtom)).toBe('bbb');
      expect(result.current.isUnknownServiceInPath).toBe(false);
    });

    // servicemap만이 아니라 serviceName을 싣는 모든 화면에서 같아야 한다.
    test('is picked up from a transactionList path too', () => {
      render({
        pathname: '/transactionList/bbb/my-app@SPRING_BOOT',
        selected: 'aaa',
        services: [DEFAULT_SERVICE, 'aaa', 'bbb'],
      });

      expect(store.get(selectedServiceAtom)).toBe('bbb');
    });

    test('is decoded before it is compared and stored', () => {
      render({ pathname: '/serviceMap/team%2Fa', selected: 'aaa', services: ['team/a'] });

      expect(store.get(selectedServiceAtom)).toBe('team/a');
    });
  });

  describe('a service that does not exist', () => {
    const renderMissing = (pathname = '/serviceMap/bbb') =>
      render({ pathname, selected: 'aaa', services: [DEFAULT_SERVICE, 'aaa'] });

    // 다른 service로 조용히 바꿔 보여주면 사용자는 자기가 요청한 것과 다른 것을 보는 줄 모른다.
    test('is reported so the caller can render 404', () => {
      const { result } = renderMissing();

      expect(result.current.isUnknownServiceInPath).toBe(true);
    });

    // 없는 이름을 반영하면 그 값이 요청 헤더와 캐시 키로까지 퍼진다.
    test('never becomes the selected service', () => {
      renderMissing();

      expect(store.get(selectedServiceAtom)).toBe('aaa');
    });

    test('is reported on a transactionList path too', () => {
      const { result } = renderMissing('/transactionList/bbb/my-app@SPRING_BOOT');

      expect(result.current.isUnknownServiceInPath).toBe(true);
    });
  });

  describe('does nothing', () => {
    // 목록이 오기 전에 없는 service로 단정하면 새로고침마다 정상 service가 DEFAULT로 튕긴다.
    test('while the service list has not arrived', () => {
      const { result } = render({
        pathname: '/serviceMap/bbb',
        selected: 'aaa',
        services: undefined,
      });

      expect(store.get(selectedServiceAtom)).toBe('aaa');
      expect(result.current.isUnknownServiceInPath).toBe(false);
    });

    test('when the path carries no service name', () => {
      const { result } = render({
        pathname: '/serverMap/my-app@SPRING_BOOT',
        selected: 'aaa',
        services: ['aaa'],
      });

      expect(store.get(selectedServiceAtom)).toBe('aaa');
      expect(result.current.isUnknownServiceInPath).toBe(false);
    });

    test('when the path already matches the selected service', () => {
      const { result } = render({
        pathname: '/serviceMap/aaa',
        selected: 'aaa',
        services: ['aaa'],
      });

      expect(store.get(selectedServiceAtom)).toBe('aaa');
      expect(result.current.isUnknownServiceInPath).toBe(false);
    });

    // 설정이 꺼져 있으면 service 개념 자체가 없으므로 404로 막아서도 안 된다.
    test('when enableServiceMap is off', () => {
      const { result } = render({
        pathname: '/serviceMap/bbb',
        selected: 'aaa',
        services: [DEFAULT_SERVICE, 'aaa'],
        enabled: false,
      });

      expect(store.get(selectedServiceAtom)).toBe('aaa');
      expect(result.current.isUnknownServiceInPath).toBe(false);
    });
  });
});
