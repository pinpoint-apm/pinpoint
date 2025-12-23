import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { chartsBoardSizesAtom } from './chartsBoard';

describe('Test chartsBoard atom', () => {
  describe('Test "chartsBoardSizesAtom"', () => {
    test('should initialize with default values', () => {
      const { result } = renderHook(() => useAtom(chartsBoardSizesAtom));
      expect(result.current[0]).toEqual(['auto', 500]);
    });

    test('should update with string width and number height', () => {
      const { result } = renderHook(() => useAtom(chartsBoardSizesAtom));

      act(() => {
        result.current[1](['100%', 600]);
      });

      expect(result.current[0]).toEqual(['100%', 600]);
    });

    test('should update with number width and number height', () => {
      const { result } = renderHook(() => useAtom(chartsBoardSizesAtom));

      act(() => {
        result.current[1]([800, 600]);
      });

      expect(result.current[0]).toEqual([800, 600]);
    });

    test('should update with auto width', () => {
      const { result } = renderHook(() => useAtom(chartsBoardSizesAtom));

      act(() => {
        result.current[1](['auto', 700]);
      });

      expect(result.current[0]).toEqual(['auto', 700]);
    });

    test('should update multiple times', () => {
      const { result } = renderHook(() => useAtom(chartsBoardSizesAtom));

      act(() => {
        result.current[1]([1000, 800]);
        result.current[1]([1200, 900]);
      });

      expect(result.current[0]).toEqual([1200, 900]);
    });

    test('should reset to default values', () => {
      const { result } = renderHook(() => useAtom(chartsBoardSizesAtom));

      act(() => {
        result.current[1]([1000, 800]);
        result.current[1](['auto', 500]);
      });

      expect(result.current[0]).toEqual(['auto', 500]);
    });
  });
});

