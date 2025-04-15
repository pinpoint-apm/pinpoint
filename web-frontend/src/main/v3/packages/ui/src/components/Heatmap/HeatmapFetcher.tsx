import React from 'react';
import HeatmapChartCore, { HeatmapChartCoreProps } from './core/HeatmapChartCore';
import {
  useServerMapSearchParameters,
  useStoragedAxisY,
  useGetHeatmapAppData,
} from '@pinpoint-fe/ui/src/hooks';
import {
  APP_SETTING_KEYS,
  ApplicationType,
  GetHeatmapAppData,
  GetServerMap,
} from '@pinpoint-fe/ui/src/constants';

export interface HeatmapFetcherHandle {
  handleCaptureImage: () => Promise<void>;
}

export type HeatmapFetcherProps = {
  nodeData: GetServerMap.NodeData | ApplicationType;
  agentId?: string;
} & Pick<HeatmapChartCoreProps, 'toolbarOption'>;

export const HeatmapFetcher = ({ nodeData, agentId, ...props }: HeatmapFetcherProps) => {
  const { dateRange } = useServerMapSearchParameters();

  const [y] = useStoragedAxisY(APP_SETTING_KEYS.HEATMAP_Y_AXIS_MIN_MAX, [0, 10000]);

  const [parameters, setParameters] = React.useState<GetHeatmapAppData.Parameters>({
    applicationName: nodeData?.applicationName,
    from: dateRange.from.getTime(),
    to: dateRange.to.getTime(),
    minElapsedTime: Number(y[0]),
    maxElapsedTime: Number(y[1]),
    agentId: agentId,
  });
  const { data, isLoading } = useGetHeatmapAppData(parameters);

  React.useEffect(() => {
    setParameters({
      applicationName: nodeData?.applicationName,
      from: dateRange.from.getTime(),
      to: dateRange.to.getTime(),
      minElapsedTime: Number(y[0]),
      maxElapsedTime: Number(y[1]),
      agentId: agentId,
    });
  }, [
    dateRange.from.getTime(),
    dateRange.to.getTime(),
    y[0],
    y[1],
    nodeData?.applicationName,
    agentId,
  ]);

  return <HeatmapChartCore isLoading={isLoading} data={data} {...props} />;
};
