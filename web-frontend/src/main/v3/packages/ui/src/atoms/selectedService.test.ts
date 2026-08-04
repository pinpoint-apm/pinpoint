import { act, renderHook } from '@testing-library/react';
import { useAtom, useAtomValue } from 'jotai';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';
import {
  DEFAULT_SERVICE,
  RESERVED_SERVICE_NAMES,
  SELECTED_SERVICE_STORAGE_KEY,
  isReservedServiceName,
  selectedServiceAtom,
} from './selectedService';

const LAST_SELECTED_SERVICE_KEY = APP_SETTING_KEYS.LAST_SELECTED_SERVICE;

// 모듈 평가 시점(getOnInit)에 저장소를 읽으므로, 저장소 상태별 동작은 모듈을 다시 평가해서 본다.
const renderWithFreshModule = () => {
  let value: string | undefined;

  jest.isolateModules(() => {
    const reloaded = require('./selectedService');
    const { result } = renderHook(() => useAtomValue(reloaded.selectedServiceAtom));
    value = result.current;
  });

  return value;
};

beforeEach(() => {
  sessionStorage.clear();
  localStorage.clear();
});

describe('isReservedServiceName', () => {
  test('returns true for every reserved name', () => {
    RESERVED_SERVICE_NAMES.forEach((name) => {
      expect(isReservedServiceName(name)).toBe(true);
    });
  });

  test('is case-insensitive', () => {
    expect(isReservedServiceName('default')).toBe(true);
    expect(isReservedServiceName('Default')).toBe(true);
    expect(isReservedServiceName('unknown')).toBe(true);
  });

  test('returns false for a normal service name', () => {
    expect(isReservedServiceName('my-service')).toBe(false);
    expect(isReservedServiceName('defaults')).toBe(false);
  });

  test('DEFAULT_SERVICE is itself reserved', () => {
    expect(isReservedServiceName(DEFAULT_SERVICE)).toBe(true);
  });
});

describe('selectedServiceAtom', () => {
  test('initializes with DEFAULT_SERVICE', () => {
    const { result } = renderHook(() => useAtomValue(selectedServiceAtom));
    expect(result.current).toBe(DEFAULT_SERVICE);
  });

  // 서드파티 스토리지가 차단된 iframe 등에서는 sessionStorage 접근 자체가 던진다.
  // getOnInit이 켜져 있으면 atomWithStorage가 모듈 평가 시점에 storage.getItem을 호출하므로,
  // 가드가 없으면 import만으로 앱 전체가 죽는다. 그래서 모듈을 다시 평가해서 확인한다.
  test('keeps working when storage access throws', () => {
    const descriptor = Object.getOwnPropertyDescriptor(window, 'sessionStorage');

    Object.defineProperty(window, 'sessionStorage', {
      configurable: true,
      get() {
        throw new Error('storage blocked');
      },
    });

    try {
      jest.isolateModules(() => {
        const reloaded = require('./selectedService');

        const { result } = renderHook(() => useAtomValue(reloaded.selectedServiceAtom));
        expect(result.current).toBe(DEFAULT_SERVICE);
      });
    } finally {
      if (descriptor) {
        Object.defineProperty(window, 'sessionStorage', descriptor);
      }
    }
  });

  test('persists the selection to both session and local storage', () => {
    jest.isolateModules(() => {
      const reloaded = require('./selectedService');
      const { result } = renderHook(() =>
        useAtom(reloaded.selectedServiceAtom as typeof selectedServiceAtom),
      );

      act(() => {
        result.current[1]('my-service');
      });
    });

    expect(sessionStorage.getItem(SELECTED_SERVICE_STORAGE_KEY)).toBe(JSON.stringify('my-service'));
    expect(localStorage.getItem(LAST_SELECTED_SERVICE_KEY)).toBe(JSON.stringify('my-service'));
  });

  // 새 탭·브라우저 재기동: sessionStorage는 비어 있고 마지막 선택만 남아 있다.
  test('falls back to the last selected service when session storage is empty', () => {
    localStorage.setItem(LAST_SELECTED_SERVICE_KEY, JSON.stringify('my-service'));

    expect(renderWithFreshModule()).toBe('my-service');
  });

  test('prefers the session value over the last selected service', () => {
    sessionStorage.setItem(SELECTED_SERVICE_STORAGE_KEY, JSON.stringify('tab-service'));
    localStorage.setItem(LAST_SELECTED_SERVICE_KEY, JSON.stringify('other-tab-service'));

    expect(renderWithFreshModule()).toBe('tab-service');
  });

  // 승계한 값은 이 탭의 sessionStorage에 심어 둔다. 그래야 다른 탭이 service를 바꾼 뒤
  // 이 탭을 새로고침해도 선택이 따라 바뀌지 않는다.
  test('pins the inherited service to this tab', () => {
    localStorage.setItem(LAST_SELECTED_SERVICE_KEY, JSON.stringify('my-service'));

    renderWithFreshModule();
    expect(sessionStorage.getItem(SELECTED_SERVICE_STORAGE_KEY)).toBe(JSON.stringify('my-service'));

    // 다른 탭에서 service를 바꾼 상황
    localStorage.setItem(LAST_SELECTED_SERVICE_KEY, JSON.stringify('other-tab-service'));

    expect(renderWithFreshModule()).toBe('my-service');
  });

  // 읽기는 되지만 쓰기가 던지는 환경(quota 초과 등)이 있다. 탭 고정 쓰기는 모듈 평가 중에
  // 일어나므로, 막지 않으면 import만으로 앱이 죽는다.
  test('keeps working when pinning the inherited service throws', () => {
    localStorage.setItem(LAST_SELECTED_SERVICE_KEY, JSON.stringify('my-service'));

    const descriptor = Object.getOwnPropertyDescriptor(window, 'sessionStorage');

    Object.defineProperty(window, 'sessionStorage', {
      configurable: true,
      get: () => ({
        getItem: () => null,
        setItem: () => {
          throw new Error('quota exceeded');
        },
        removeItem: () => {},
      }),
    });

    try {
      expect(renderWithFreshModule()).toBe('my-service');
    } finally {
      if (descriptor) {
        Object.defineProperty(window, 'sessionStorage', descriptor);
      }
    }
  });

  test('keeps working when local storage access throws', () => {
    const descriptor = Object.getOwnPropertyDescriptor(window, 'localStorage');

    Object.defineProperty(window, 'localStorage', {
      configurable: true,
      get() {
        throw new Error('storage blocked');
      },
    });

    try {
      jest.isolateModules(() => {
        const reloaded = require('./selectedService');
        const { result } = renderHook(() =>
          useAtom(reloaded.selectedServiceAtom as typeof selectedServiceAtom),
        );

        expect(result.current[0]).toBe(DEFAULT_SERVICE);

        // 마지막 선택을 기록하지 못해도 이 탭의 선택은 그대로 동작해야 한다.
        act(() => {
          result.current[1]('my-service');
        });

        expect(result.current[0]).toBe('my-service');
      });

      expect(sessionStorage.getItem(SELECTED_SERVICE_STORAGE_KEY)).toBe(
        JSON.stringify('my-service'),
      );
    } finally {
      if (descriptor) {
        Object.defineProperty(window, 'localStorage', descriptor);
      }
    }
  });
});
