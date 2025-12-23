import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { urlSelectedSummaryDataAtom } from './url';
import { UrlStatSummary } from '@pinpoint-fe/ui/src/constants';

describe('Test url atom', () => {
  describe('Test "urlSelectedSummaryDataAtom"', () => {
    test('should initialize with empty object', () => {
      const { result } = renderHook(() => useAtom(urlSelectedSummaryDataAtom));
      expect(result.current[0]).toEqual({} as UrlStatSummary.SummaryData);
    });

    test('should update with summary data', () => {
      const { result } = renderHook(() => useAtom(urlSelectedSummaryDataAtom));
      const mockData: UrlStatSummary.SummaryData = {
        url: '/test',
        totalCount: 100,
        failureCount: 5,
        avgElapsed: 50,
      } as UrlStatSummary.SummaryData;

      act(() => {
        result.current[1](mockData);
      });

      expect(result.current[0]).toEqual(mockData);
    });

    test('should update with different summary data', () => {
      const { result } = renderHook(() => useAtom(urlSelectedSummaryDataAtom));
      const mockData1: UrlStatSummary.SummaryData = {
        url: '/test1',
        totalCount: 100,
      } as UrlStatSummary.SummaryData;

      const mockData2: UrlStatSummary.SummaryData = {
        url: '/test2',
        totalCount: 200,
      } as UrlStatSummary.SummaryData;

      act(() => {
        result.current[1](mockData1);
        result.current[1](mockData2);
      });

      expect(result.current[0]).toEqual(mockData2);
    });

    test('should reset to empty object', () => {
      const { result } = renderHook(() => useAtom(urlSelectedSummaryDataAtom));
      const mockData: UrlStatSummary.SummaryData = {
        url: '/test',
        totalCount: 100,
      } as UrlStatSummary.SummaryData;

      act(() => {
        result.current[1](mockData);
        result.current[1]({} as UrlStatSummary.SummaryData);
      });

      expect(result.current[0]).toEqual({} as UrlStatSummary.SummaryData);
    });
  });
});

