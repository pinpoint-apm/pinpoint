import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { globalSearchDisplayAtom } from './globalSearch';

describe('Test globalSearch atom', () => {
  describe('Test "globalSearchDisplayAtom"', () => {
    test('should initialize with false', () => {
      const { result } = renderHook(() => useAtom(globalSearchDisplayAtom));
      expect(result.current[0]).toBe(false);
    });

    test('should update to true', () => {
      const { result } = renderHook(() => useAtom(globalSearchDisplayAtom));

      act(() => {
        result.current[1](true);
      });

      expect(result.current[0]).toBe(true);
    });

    test('should toggle between true and false', () => {
      const { result } = renderHook(() => useAtom(globalSearchDisplayAtom));

      act(() => {
        result.current[1](true);
        result.current[1](false);
        result.current[1](true);
      });

      expect(result.current[0]).toBe(true);
    });

    test('should reset to false', () => {
      const { result } = renderHook(() => useAtom(globalSearchDisplayAtom));

      act(() => {
        result.current[1](true);
        result.current[1](false);
      });

      expect(result.current[0]).toBe(false);
    });
  });
});

