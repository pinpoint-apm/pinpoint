import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { toastCountAtom } from './toast';

describe('Test toast atom', () => {
  describe('Test "toastCountAtom"', () => {
    test('should initialize with 0', () => {
      const { result } = renderHook(() => useAtom(toastCountAtom));
      expect(result.current[0]).toBe(0);
    });

    test('should increment count', () => {
      const { result } = renderHook(() => useAtom(toastCountAtom));

      act(() => {
        result.current[1](1);
      });

      expect(result.current[0]).toBe(1);
    });

    test('should update to specific count', () => {
      const { result } = renderHook(() => useAtom(toastCountAtom));

      act(() => {
        result.current[1](5);
      });

      expect(result.current[0]).toBe(5);
    });

    test('should increment multiple times', () => {
      const { result } = renderHook(() => useAtom(toastCountAtom));

      act(() => {
        result.current[1](1);
        result.current[1](2);
        result.current[1](3);
      });

      expect(result.current[0]).toBe(3);
    });

    test('should reset to 0', () => {
      const { result } = renderHook(() => useAtom(toastCountAtom));

      act(() => {
        result.current[1](5);
        result.current[1](0);
      });

      expect(result.current[0]).toBe(0);
    });

    test('should handle negative values', () => {
      const { result } = renderHook(() => useAtom(toastCountAtom));

      act(() => {
        result.current[1](-1);
      });

      expect(result.current[0]).toBe(-1);
    });
  });
});

