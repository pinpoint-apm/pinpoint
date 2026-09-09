import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import { configurationAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';
import { useRequestService } from './useRequestService';

const mockLocation = { pathname: '/serviceMap/test-app@SPRING_BOOT' };
jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  useLocation: () => mockLocation,
}));

const store = getDefaultStore();

const configWithServiceMap = (enable: boolean) =>
  ({ 'experimental.enableServiceMap.value': enable }) as unknown as Configuration;

const renderRequestService = (pathname: string, configuration?: Configuration) => {
  mockLocation.pathname = pathname;
  act(() => {
    store.set(configurationAtom, configuration);
  });
  return renderHook(() => useRequestService()).result.current;
};

describe('useRequestService', () => {
  beforeEach(() => {
    act(() => {
      store.set(selectedServiceAtom, 'my-service');
      store.set(configurationAtom, undefined);
    });
  });

  test('returns the selected service on a service scoped page', () => {
    expect(
      renderRequestService('/serviceMap/test-app@SPRING_BOOT', configWithServiceMap(true)),
    ).toBe('my-service');
  });

  // servicemap은 serviceName을 경로에 실으므로, 그 값이 전역 선택값을 이겨야 한다.
  test('prefers the service name carried as a servicemap path segment', () => {
    expect(renderRequestService('/serviceMap/blogService', configWithServiceMap(true))).toBe(
      'blogService',
    );
    expect(
      renderRequestService('/serviceMap/DEFAULT/test-app@SPRING_BOOT', configWithServiceMap(true)),
    ).toBe('DEFAULT');
  });

  test('returns undefined when enableServiceMap is off', () => {
    expect(
      renderRequestService('/serviceMap/test-app@SPRING_BOOT', configWithServiceMap(false)),
    ).toBeUndefined();
  });

  test('returns undefined when no configuration is loaded yet', () => {
    // configuration은 부트스트랩 이후 비동기로 로드되므로 아직 없을 수 있다.
    expect(renderRequestService('/serviceMap/test-app@SPRING_BOOT')).toBeUndefined();
  });

  test('returns the selected service on the servermap page too', () => {
    // ServerMap도 예외가 아니라 선택된 service 범위에서 조회된다.
    const config = configWithServiceMap(true);
    expect(renderRequestService('/serverMap/test-app@SPRING_BOOT', config)).toBe('my-service');
    expect(renderRequestService('/serverMap/realtime/test-app@SPRING_BOOT', config)).toBe(
      'my-service',
    );
  });

  test('prefers the service name already carried by the path', () => {
    expect(
      renderRequestService(
        '/transactionList/url-service/test-app@SPRING_BOOT',
        configWithServiceMap(true),
      ),
    ).toBe('url-service');
  });

  test('falls back to the selected service on a legacy transactionList path', () => {
    expect(
      renderRequestService('/transactionList/test-app@SPRING_BOOT', configWithServiceMap(true)),
    ).toBe('my-service');
  });

  // 규칙의 마지막 단계. 경로에도 전역에도 service가 없으면 DEFAULT다 — 백엔드가 헤더 없는
  // 요청을 해석하는 값과 같아서, 호출부가 `?? DEFAULT_SERVICE`를 다시 붙일 필요가 없다.
  test('falls back to DEFAULT when neither the path nor the global selection has a service', () => {
    act(() => {
      store.set(selectedServiceAtom, '');
    });

    expect(
      renderRequestService('/serverMap/test-app@SPRING_BOOT', configWithServiceMap(true)),
    ).toBe('DEFAULT');
  });
});
