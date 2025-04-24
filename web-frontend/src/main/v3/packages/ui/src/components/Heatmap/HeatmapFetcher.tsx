import React from 'react';
import HeatmapChartCore, { HeatmapChartCoreProps } from './core/HeatmapChartCore';
import {
  useServerMapSearchParameters,
  useStoragedSetting,
  useGetHeatmapAppData,
} from '@pinpoint-fe/ui/src/hooks';
import { APP_SETTING_KEYS, GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';

const DefaultAxisY = [0, 10000];

export type HeatmapFetcherProps = {
  agentId?: string;
} & Pick<HeatmapChartCoreProps, 'toolbarOption' | 'nodeData'>;

export const HeatmapFetcher = ({ nodeData, agentId, ...props }: HeatmapFetcherProps) => {
  const { dateRange } = useServerMapSearchParameters();
  const [setting] = useStoragedSetting(APP_SETTING_KEYS.HEATMAP_SETTING);

  const [parameters, setParameters] = React.useState<GetHeatmapAppData.Parameters>({
    applicationName: nodeData?.applicationName,
    from: dateRange.from.getTime(),
    to: dateRange.to.getTime(),
    minElapsedTime: Number(setting?.yMin) || DefaultAxisY[0],
    maxElapsedTime: Number(setting?.yMax) || DefaultAxisY[1],
    agentId: agentId,
  });
  const { data, isLoading } = useGetHeatmapAppData(parameters);

  React.useEffect(() => {
    setParameters({
      applicationName: nodeData?.applicationName,
      from: dateRange.from.getTime(),
      to: dateRange.to.getTime(),
      minElapsedTime: Number(setting?.yMin) || DefaultAxisY[0],
      maxElapsedTime: Number(setting?.yMax) || DefaultAxisY[1],
      agentId: agentId,
    });
  }, [
    dateRange.from.getTime(),
    dateRange.to.getTime(),
    setting?.yMin,
    setting?.yMax,
    nodeData?.applicationName,
    agentId,
  ]);

  return <HeatmapChartCore isLoading={isLoading} data={data} nodeData={nodeData} {...props} />;
};
