import React from 'react';
import { useLocalStorage } from '@pinpoint-fe/ui/src/hooks';
import { DefaultValue } from '@pinpoint-fe/ui/src/components/Heatmap/core/HeatmapSetting';

export type HeatmapSettingType = {
  yMin: number;
  yMax: number;
  visualMapSuccessMax?: number;
  visualMapFailMax?: number;
};

export const useStoragedSetting = (
  storageKey: string,
  initialValue = DefaultValue,
): [HeatmapSettingType, React.Dispatch<React.SetStateAction<HeatmapSettingType>>] => {
  const storagedAxisY = useLocalStorage<HeatmapSettingType>(
    storageKey,
    initialValue as HeatmapSettingType,
  );

  return storagedAxisY;
};
