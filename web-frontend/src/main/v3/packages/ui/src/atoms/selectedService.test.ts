import { renderHook } from '@testing-library/react';
import { useAtomValue } from 'jotai';
import {
  DEFAULT_SERVICE,
  RESERVED_SERVICE_NAMES,
  isReservedServiceName,
  selectedServiceAtom,
} from './selectedService';

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
});
