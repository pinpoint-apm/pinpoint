import React from 'react';
import { useLocalStorage } from '@pinpoint-fe/ui/src/hooks';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';

type AxisRangeType = [number, number];

export const useStoragedAxisY = (
  initialValue = [0, 10000],
): [AxisRangeType, React.Dispatch<React.SetStateAction<AxisRangeType>>] => {
  const storagedAxisY = useLocalStorage<AxisRangeType>(
    APP_SETTING_KEYS.SCATTER_Y_AXIS_MIN_MAX,
    initialValue as AxisRangeType,
  );

  return storagedAxisY;
};
