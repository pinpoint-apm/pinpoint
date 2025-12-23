import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { searchParametersAtom } from './searchParameters';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';

describe('Test searchParameters atom', () => {
  describe('Test "searchParametersAtom"', () => {
    test('should initialize with empty application and searchParameters', () => {
      const { result } = renderHook(() => useAtom(searchParametersAtom));
      expect(result.current[0]).toEqual({
        application: {} as ApplicationType,
        searchParameters: {},
      });
    });

    test('should update with application and searchParameters', () => {
      const { result } = renderHook(() => useAtom(searchParametersAtom));
      const mockApplication: ApplicationType = {
        applicationName: 'app1',
        serviceType: 'SPRING_BOOT',
      } as ApplicationType;

      act(() => {
        result.current[1]({
          application: mockApplication,
          searchParameters: {
            query: 'test',
            filter: 'active',
          },
        });
      });

      expect(result.current[0]).toEqual({
        application: mockApplication,
        searchParameters: {
          query: 'test',
          filter: 'active',
        },
      });
    });

    test('should update with different searchParameters', () => {
      const { result } = renderHook(() => useAtom(searchParametersAtom));
      const mockApplication: ApplicationType = {
        applicationName: 'app1',
        serviceType: 'SPRING_BOOT',
      } as ApplicationType;

      act(() => {
        result.current[1]({
          application: mockApplication,
          searchParameters: {
            query: 'test1',
          },
        });
        result.current[1]({
          application: mockApplication,
          searchParameters: {
            query: 'test2',
            filter: 'inactive',
          },
        });
      });

      expect(result.current[0]).toEqual({
        application: mockApplication,
        searchParameters: {
          query: 'test2',
          filter: 'inactive',
        },
      });
    });

    test('should update with different application', () => {
      const { result } = renderHook(() => useAtom(searchParametersAtom));
      const mockApplication1: ApplicationType = {
        applicationName: 'app1',
        serviceType: 'SPRING_BOOT',
      } as ApplicationType;

      const mockApplication2: ApplicationType = {
        applicationName: 'app2',
        serviceType: 'TOMCAT',
      } as ApplicationType;

      act(() => {
        result.current[1]({
          application: mockApplication1,
          searchParameters: {
            query: 'test',
          },
        });
        result.current[1]({
          application: mockApplication2,
          searchParameters: {
            query: 'test',
          },
        });
      });

      expect(result.current[0]).toEqual({
        application: mockApplication2,
        searchParameters: {
          query: 'test',
        },
      });
    });

    test('should reset to empty values', () => {
      const { result } = renderHook(() => useAtom(searchParametersAtom));
      const mockApplication: ApplicationType = {
        applicationName: 'app1',
        serviceType: 'SPRING_BOOT',
      } as ApplicationType;

      act(() => {
        result.current[1]({
          application: mockApplication,
          searchParameters: {
            query: 'test',
          },
        });
        result.current[1]({
          application: {} as ApplicationType,
          searchParameters: {},
        });
      });

      expect(result.current[0]).toEqual({
        application: {} as ApplicationType,
        searchParameters: {},
      });
    });
  });
});

