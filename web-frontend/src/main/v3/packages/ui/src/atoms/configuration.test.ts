import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { configurationAtom } from './configuration';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

describe('Test configuration atom', () => {
  describe('Test "configurationAtom"', () => {
    test('should initialize with undefined', () => {
      const { result } = renderHook(() => useAtom(configurationAtom));
      expect(result.current[0]).toBeUndefined();
    });

    test('should update with configuration data', () => {
      const { result } = renderHook(() => useAtom(configurationAtom));
      const mockConfig: Configuration = {
        enableServerMapRealTime: true,
        showApplicationStat: true,
      } as Configuration;

      act(() => {
        result.current[1](mockConfig);
      });

      expect(result.current[0]).toEqual(mockConfig);
    });

    test('should update with different configuration data', () => {
      const { result } = renderHook(() => useAtom(configurationAtom));
      const mockConfig1: Configuration = {
        enableServerMapRealTime: true,
        showApplicationStat: true,
      } as Configuration;

      const mockConfig2: Configuration = {
        enableServerMapRealTime: false,
        showApplicationStat: false,
      } as Configuration;

      act(() => {
        result.current[1](mockConfig1);
        result.current[1](mockConfig2);
      });

      expect(result.current[0]).toEqual(mockConfig2);
    });

    test('should reset to undefined', () => {
      const { result } = renderHook(() => useAtom(configurationAtom));
      const mockConfig: Configuration = {
        enableServerMapRealTime: true,
      } as Configuration;

      act(() => {
        result.current[1](mockConfig);
        result.current[1](undefined);
      });

      expect(result.current[0]).toBeUndefined();
    });
  });
});

