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
});
