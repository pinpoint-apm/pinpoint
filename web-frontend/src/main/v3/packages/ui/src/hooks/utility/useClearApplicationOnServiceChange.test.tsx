import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import {
  DEFAULT_SERVICE,
  searchParametersAtom,
  selectedServiceAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';
import {
  resolveClearedApplicationPath,
  useClearApplicationOnServiceChange,
} from './useClearApplicationOnServiceChange';

const mockNavigate = jest.fn();
const mockLocation = { pathname: '/config/serviceSetting' };
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  useLocation: () => mockLocation,
}));

describe('resolveClearedApplicationPath', () => {
  test('strips the application segment on the serverMap path', () => {
    expect(resolveClearedApplicationPath('/serverMap/test-app@SPRING_BOOT')).toBe('/serverMap');
  });

  test('strips the application segment on any page path', () => {
    expect(resolveClearedApplicationPath('/inspector/test-app@SPRING_BOOT')).toBe('/inspector');
  });

  test('handles the `^` separated application key form', () => {
    expect(resolveClearedApplicationPath('/serverMap/test-app^SPRING_BOOT')).toBe('/serverMap');
  });

  test('keeps the basename prefix', () => {
    expect(resolveClearedApplicationPath('/base/serverMap/test-app@SPRING_BOOT')).toBe(
      '/base/serverMap',
    );
  });

  test('returns null when there is no application segment', () => {
    expect(resolveClearedApplicationPath('/serverMap')).toBeNull();
    expect(resolveClearedApplicationPath('/config/general')).toBeNull();
  });

  test('falls back to root when stripping leaves an empty path', () => {
    expect(resolveClearedApplicationPath('test-app@SPRING_BOOT')).toBe('/');
  });
});

// 리다이렉트 경로 계산은 위 resolveClearedApplicationPath 테스트가 커버한다.
// 여기서는 관측 가능한 부분(searchParametersAtom의 application 무효화, soft navigate 호출)과
// 게이팅 동작을 검증한다.
describe('useClearApplicationOnServiceChange', () => {
  const store = getDefaultStore();

  const APP: ApplicationType = { applicationName: 'old-app', serviceType: 'SPRING_BOOT' };

  const renderClearHook = (enabled: boolean) =>
    renderHook(({ enabled }) => useClearApplicationOnServiceChange(enabled), {
      initialProps: { enabled },
    });

  beforeEach(() => {
    mockNavigate.mockClear();
    mockLocation.pathname = '/config/serviceSetting';
    act(() => {
      store.set(selectedServiceAtom, DEFAULT_SERVICE);
      store.set(searchParametersAtom, { application: APP, searchParameters: {} });
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

  test('soft-navigates to the cleared path when an application is in the URL', () => {
    mockLocation.pathname = '/serverMap/old-app@SPRING_BOOT';
    renderClearHook(true);
    act(() => {
      store.set(selectedServiceAtom, 'svc-a');
    });
    expect(mockNavigate).toHaveBeenCalledWith('/serverMap', { replace: true });
  });

  test('does not navigate when there is no application in the URL', () => {
    renderClearHook(true);
    act(() => {
      store.set(selectedServiceAtom, 'svc-a');
    });
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
