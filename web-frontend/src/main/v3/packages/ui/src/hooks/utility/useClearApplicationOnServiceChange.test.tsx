import { renderHook, act } from '@testing-library/react';
import { getDefaultStore } from 'jotai';
import {
  DEFAULT_SERVICE,
  searchParametersAtom,
  selectedServiceAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { useClearApplicationOnServiceChange } from './useClearApplicationOnServiceChange';

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

// 관측 가능한 부분(searchParametersAtom의 application 무효화, servicemap으로의 soft navigate)과
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

  test('soft-navigates to the servicemap without an application segment', () => {
    renderClearHook(true);
    act(() => {
      store.set(selectedServiceAtom, 'svc-a');
    });
    expect(mockNavigate).toHaveBeenCalledWith(APP_PATH.SERVICE_MAP, { replace: true });
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

  test('does not navigate when the service value is set but unchanged', () => {
    renderClearHook(true);
    act(() => {
      store.set(selectedServiceAtom, DEFAULT_SERVICE);
    });
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
