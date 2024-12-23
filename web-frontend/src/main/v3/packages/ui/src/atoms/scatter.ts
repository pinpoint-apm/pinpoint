import { atom } from 'jotai';

import {
  FilteredMap,
  GetScatter,
  SCATTER_DATA_TOTAL_KEY,
  ScatterDataByAgent,
} from '@pinpoint-fe/constants';
import { getScatterData, getMergedKeys } from '@pinpoint-fe/ui/utils';

const scatterDataRootAtom = atom<ScatterDataByAgent>({
  curr: {
    [SCATTER_DATA_TOTAL_KEY]: [],
  },
  acc: {
    [SCATTER_DATA_TOTAL_KEY]: [],
  },
});

export const scatterDataAtom = atom(
  (get) => {
    return get(scatterDataRootAtom);
  },
  (get, set, newData?: GetScatter.Response) => {
    if (newData) {
      const prevData = get(scatterDataRootAtom);
      const result = getScatterData(newData, prevData);
      set(scatterDataRootAtom, result);
    } else {
      set(scatterDataRootAtom, {
        curr: {
          [SCATTER_DATA_TOTAL_KEY]: [],
        },
        acc: {
          [SCATTER_DATA_TOTAL_KEY]: [],
        },
      });
    }
  },
);

const scatterDataByApplicationKeyRootAtom = atom<{ [key: string]: ScatterDataByAgent } | undefined>(
  undefined,
);

export const scatterDataByApplicationKeyAtom = atom(
  (get) => {
    return get(scatterDataByApplicationKeyRootAtom);
  },
  (get, set, newData: FilteredMap.ApplicationScatterData | undefined) => {
    if (newData) {
      const prevData = get(scatterDataByApplicationKeyRootAtom);
      const mergedKeys = newData ? getMergedKeys(prevData, newData) : undefined;
      const resultData = mergedKeys?.reduce((acc, key) => {
        const prevScatterData = prevData?.[key];
        const newScatterData = newData[key]
          ? getScatterData(newData[key], prevScatterData, { isFilterMap: true })
          : prevScatterData;
        return {
          ...acc,
          [key]: newScatterData,
        };
      }, {});
      set(scatterDataByApplicationKeyRootAtom, resultData);
    } else {
      set(scatterDataByApplicationKeyRootAtom, undefined);
    }
  },
);
