import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { userMetricConfigAtom } from './openTelemetry';

describe('Test openTelemetry atom', () => {
  describe('Test "userMetricConfigAtom"', () => {
    test('should initialize with empty object', () => {
      const { result } = renderHook(() => useAtom(userMetricConfigAtom));
      expect(result.current[0]).toEqual({});
    });

    test('should update with title only', () => {
      const { result } = renderHook(() => useAtom(userMetricConfigAtom));

      act(() => {
        result.current[1]({ title: 'Test Metric' });
      });

      expect(result.current[0]).toEqual({ title: 'Test Metric' });
    });

    test('should update with unit only', () => {
      const { result } = renderHook(() => useAtom(userMetricConfigAtom));

      act(() => {
        result.current[1]({ unit: 'ms' });
      });

      expect(result.current[0]).toEqual({ unit: 'ms' });
    });

    test('should update with chartType only', () => {
      const { result } = renderHook(() => useAtom(userMetricConfigAtom));

      act(() => {
        result.current[1]({ chartType: 'line' });
      });

      expect(result.current[0]).toEqual({ chartType: 'line' });
    });

    test('should update with all properties', () => {
      const { result } = renderHook(() => useAtom(userMetricConfigAtom));

      act(() => {
        result.current[1]({
          title: 'Response Time',
          unit: 'ms',
          chartType: 'bar',
        });
      });

      expect(result.current[0]).toEqual({
        title: 'Response Time',
        unit: 'ms',
        chartType: 'bar',
      });
    });

    test('should update with partial properties', () => {
      const { result } = renderHook(() => useAtom(userMetricConfigAtom));

      act(() => {
        result.current[1]({
          title: 'Response Time',
          unit: 'ms',
        });
        result.current[1]({
          title: 'Response Time',
          unit: 'ms',
          chartType: 'line',
        });
      });

      expect(result.current[0]).toEqual({
        title: 'Response Time',
        unit: 'ms',
        chartType: 'line',
      });
    });

    test('should reset to empty object', () => {
      const { result } = renderHook(() => useAtom(userMetricConfigAtom));

      act(() => {
        result.current[1]({
          title: 'Response Time',
          unit: 'ms',
          chartType: 'bar',
        });
        result.current[1]({});
      });

      expect(result.current[0]).toEqual({});
    });
  });
});

