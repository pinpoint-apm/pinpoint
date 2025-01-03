import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { scatterDataByApplicationKeyAtom, scatterDataAtom } from './scatter';
import { newData, previousData, resultData, scatterData, totalData } from './scatterMock';

describe('Test scatter atom', () => {
  describe('Test "scatterDataByApplicationKeyAtom"', () => {
    test('Test to Ensure setScatterDataByApplicationKeyAtom Transforms Data from FilteredMap.Response["applicationScatterData"] Form to { [key: string]: ScatterDataByAgent; } Form', () => {
      const { result } = renderHook(() => useAtom(scatterDataByApplicationKeyAtom));
      act(() => {
        result.current[1](scatterData);
      });
      // Wait for the state to update
      setTimeout(() => {
        expect(result.current[0]).toStrictEqual(resultData);
      }, 0);
    });
  });

  describe('Test "scatterDataAtom"', () => {
    test('Test to Ensure setScatterDataAtom returns merged data with previous data', () => {
      const { result } = renderHook(() => useAtom(scatterDataAtom));

      act(() => {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        result.current[1](previousData as any);
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        result.current[1](newData as any);
      });
      // Wait for the state to update
      setTimeout(() => {
        expect(result.current[0]).toStrictEqual(totalData);
      }, 0);
    });
  });
});
