import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import {
  currentServerAtom,
  DEFAULT_SERVICE,
  searchParametersAtom,
  selectedServiceAtom,
  serverMapCurrentTargetAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { useClearApplicationOnServiceChange } from './useClearApplicationOnServiceChange';

const mockNavigate = jest.fn();
const mockLocation = { pathname: '/serviceMap/DEFAULT' };
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useLocation: () => mockLocation,
}));

// 관측 가능한 부분(searchParametersAtom의 application 무효화, servicemap으로의 soft navigate)과
// 게이팅 동작을 검증한다.
describe('useClearApplicationOnServiceChange', () => {
  const store = getDefaultStore();

  const APP: ApplicationType = { applicationName: 'old-app', serviceType: 'SPRING_BOOT' };
  const TARGET = {
    id: 'old-app^SPRING_BOOT',
    applicationName: 'old-app',
    serviceType: 'SPRING_BOOT',
    type: 'node' as const,
  };

  const renderClearHook = (enabled: boolean) =>
    renderHook(({ enabled }) => useClearApplicationOnServiceChange(enabled), {
      initialProps: { enabled },
    });

  beforeEach(() => {
    mockNavigate.mockClear();
    mockLocation.pathname = '/serviceMap/DEFAULT';
    act(() => {
      store.set(selectedServiceAtom, DEFAULT_SERVICE);
      store.set(searchParametersAtom, { application: APP, searchParameters: {} });
      store.set(serverMapCurrentTargetAtom, TARGET);
      store.set(currentServerAtom, undefined);
    });
  });

  // 이전 service의 map에 있던 노드를 가리키는 선택이 남으면, ChartsBoard가 그것을 기준으로
  // 조회를 시작한다. 새 경로에는 기준 application도 없어서 applicationName 없는 요청이 나간다.
  describe('the map selection made in the previous service', () => {
    test('is cleared when the service changes', () => {
      renderClearHook(true);
      act(() => {
        store.set(selectedServiceAtom, 'svc-a');
      });
      expect(store.get(serverMapCurrentTargetAtom)).toBeUndefined();
      expect(store.get(currentServerAtom)).toBeUndefined();
    });

    test('is kept on initial mount', () => {
      renderClearHook(true);
      expect(store.get(serverMapCurrentTargetAtom)).toEqual(TARGET);
    });

    test('is kept when disabled', () => {
      renderClearHook(false);
      act(() => {
        store.set(selectedServiceAtom, 'svc-a');
      });
      expect(store.get(serverMapCurrentTargetAtom)).toEqual(TARGET);
    });
  });

  test('invalidates the stored application when the service changes', () => {
    renderClearHook(true);
    act(() => {
      store.set(selectedServiceAtom, 'svc-a');
    });
    expect(store.get(searchParametersAtom).application).toEqual({});
  });

  test('does not invalidate the stored application on initial mount', () => {
    renderClearHook(true);
    expect(store.get(searchParametersAtom).application).toEqual(APP);
  });

  test('does not invalidate when disabled', () => {
    renderClearHook(false);
    act(() => {
      store.set(selectedServiceAtom, 'svc-a');
    });
    expect(store.get(searchParametersAtom).application).toEqual(APP);
  });

  test('does not invalidate when the service value is set but unchanged', () => {
    renderClearHook(true);
    act(() => {
      store.set(selectedServiceAtom, DEFAULT_SERVICE);
    });
    expect(store.get(searchParametersAtom).application).toEqual(APP);
  });

  // serviceName을 경로에 실어야 새 화면의 모든 조회가 URL 기준으로 해석된다.
  test('soft-navigates to the new service servicemap without an application segment', () => {
    renderClearHook(true);
    act(() => {
      store.set(selectedServiceAtom, 'svc-a');
    });
    expect(mockNavigate).toHaveBeenCalledWith(`${APP_PATH.SERVICE_MAP}/svc-a`, { replace: true });
  });

  test('encodes a service name that would otherwise break the path', () => {
    renderClearHook(true);
    act(() => {
      store.set(selectedServiceAtom, 'team/a@b');
    });
    expect(mockNavigate).toHaveBeenCalledWith(`${APP_PATH.SERVICE_MAP}/team%2Fa%40b`, {
      replace: true,
    });
  });

  test('does not navigate on initial mount', () => {
    renderClearHook(true);
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  test('does not navigate when disabled', () => {
    renderClearHook(false);
    act(() => {
      store.set(selectedServiceAtom, 'svc-a');
    });
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  // 경로가 이미 새 service를 가리키면 사용자가 주소창에서 바꿔 들어온 것이다.
  // (`useSyncSelectedServiceWithPath`가 그 값을 전역 선택값에 반영한다.)
  describe('when the path already points at the new service', () => {
    beforeEach(() => {
      mockLocation.pathname = '/transactionList/svc-a/old-app@SPRING_BOOT';
    });

    test('does not navigate away from the screen the user asked for', () => {
      renderClearHook(true);
      act(() => {
        store.set(selectedServiceAtom, 'svc-a');
      });
      expect(mockNavigate).not.toHaveBeenCalled();
    });

    // application은 경로에서 다시 동기화되므로(InitialFetchOutlet) 비우면 오히려 어긋난다.
    test('leaves the stored application alone', () => {
      renderClearHook(true);
      act(() => {
        store.set(selectedServiceAtom, 'svc-a');
      });
      expect(store.get(searchParametersAtom).application).toEqual(APP);
    });

    // 이전 service의 map에 있던 노드를 가리키므로 이 경우에도 무효다.
    test('still clears the map selection', () => {
      renderClearHook(true);
      act(() => {
        store.set(selectedServiceAtom, 'svc-a');
      });
      expect(store.get(serverMapCurrentTargetAtom)).toBeUndefined();
    });
  });

  test('does not navigate when the service value is set but unchanged', () => {
    renderClearHook(true);
    act(() => {
      store.set(selectedServiceAtom, DEFAULT_SERVICE);
    });
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
