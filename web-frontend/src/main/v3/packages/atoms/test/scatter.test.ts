import { renderHook, act } from '@testing-library/react';
import { useAtom } from 'jotai';
import { scatterDataByApplicationKeyAtom, scatterDataAtom } from '../src/scatter';
import { newData, previousData, resultData, scatterData, totalData } from './scatterMock';

describe('Test scatter atom', () => {
  describe('Test "scatterDataByApplicationKeyAtom"', () => {
    test(`
      Test to Ensure setScatterDataByApplicationKeyAtom Transforms Data from 
      FilteredMap.Response['applicationScatterData']Form to 
      { [key: string]: ScatterDataByAgent; } Form" 
    `, () => {
      const { result } = renderHook(() => useAtom(scatterDataByApplicationKeyAtom));

      act(() => {
        result.current[1](scatterData);
      });

      expect(result.current[0]).toStrictEqual(resultData);
    });
  });

  describe('Test "scatterDataAtom"', () => {
    test(`
      Test to Ensure setScatterDataAtom returns merged data with previous data
    `, () => {
      const { result } = renderHook(() => useAtom(scatterDataAtom));

      act(() => {
        result.current[1](previousData);
        result.current[1](newData);
      });

      expect(result.current[0]).toStrictEqual(totalData);
    });
  });
});
