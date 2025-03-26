import React from 'react';
import { useLocalStorage } from '@pinpoint-fe/ui/src/hooks';

type AxisRangeType = [number, number];

export const useStoragedAxisY = (
  storageKey: string,
  initialValue = [0, 10000],
): [AxisRangeType, React.Dispatch<React.SetStateAction<AxisRangeType>>] => {
  const storagedAxisY = useLocalStorage<AxisRangeType>(storageKey, initialValue as AxisRangeType);

  return storagedAxisY;
};
