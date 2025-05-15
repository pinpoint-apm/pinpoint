import React from 'react';
import HeatmapChartCore, { HeatmapChartCoreProps } from './core/HeatmapChartCore';
import {
  useServerMapSearchParameters,
  useStoragedSetting,
  useGetHeatmapAppData,
} from '@pinpoint-fe/ui/src/hooks';
import { APP_SETTING_KEYS, GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';
import { useTranslation } from 'react-i18next';

const DefaultAxisY = [0, 10000];

export type HeatmapFetcherProps = {
  agentId?: string;
} & Pick<HeatmapChartCoreProps, 'toolbarOption' | 'nodeData'>;

export const HeatmapFetcher = ({ nodeData, agentId, ...props }: HeatmapFetcherProps) => {
  const { t } = useTranslation();
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
  const { data, isLoading, error } = useGetHeatmapAppData(parameters);

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

  return (
    <div className="relative w-full h-full">
      {error && (
        <div className="absolute inset-0 z-[1000] flex items-center justify-center">
          <div className="absolute inset-0 opacity-50 bg-background"></div>
          <div className="z-10 text-red-500">
            {t('SERVER_MAP.HEATMAP_API_ERROR_MESSAGE')
              .split('\n')
              .map((txt, i) => (
                <p key={i}>{txt}</p>
              ))}
          </div>
        </div>
      )}
      <HeatmapChartCore
        isLoading={isLoading}
        data={data || ({} as GetHeatmapAppData.Response)}
        nodeData={nodeData}
        {...props}
      />
    </div>
  );
};
