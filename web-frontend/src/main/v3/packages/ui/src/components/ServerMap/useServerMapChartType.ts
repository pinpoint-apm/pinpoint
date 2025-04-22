import React from 'react';
import { useLocalStorage } from '@pinpoint-fe/ui/src/hooks';
import { APP_SETTING_KEYS, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { useAtomValue } from 'jotai';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

type ChartType = 'scatter' | 'heatmap';

export const useServerMapChartType = (
  initialValue: ChartType = 'scatter',
): [ChartType, React.Dispatch<ChartType>] => {
  const configuration = useAtomValue(configurationAtom);
  const [enableHeatmap] = useLocalStorage(
    EXPERIMENTAL_CONFIG_KEYS.ENABLE_HEATMAP,
    !!configuration?.['experimental.enableHeatmap.value'],
  );

  if (!enableHeatmap) {
    return ['scatter', () => {}];
  }

  const charTypeStorage = useLocalStorage<ChartType>(
    APP_SETTING_KEYS.SERVER_MAP_CHART_TYPE,
    initialValue,
  );

  return charTypeStorage;
};
