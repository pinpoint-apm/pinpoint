import HeatmapChartCore, { HeatmapChartCoreProps } from './core/HeatmapChartCore';
import {
  useServerMapSearchParameters,
  useStoragedSetting,
  useGetHeatmapAppData,
} from '@pinpoint-fe/ui/src/hooks';
import { APP_SETTING_KEYS, GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';
import { toBasicISOString } from '@pinpoint-fe/ui/src/utils';
import { useTranslation } from 'react-i18next';

const DefaultAxisY = [0, 10000];

export type HeatmapFetcherProps = {
  agentId?: string;
} & Pick<HeatmapChartCoreProps, 'toolbarOption' | 'nodeData' | 'serviceName'>;

export const HeatmapFetcher = ({
  nodeData,
  agentId,
  serviceName,
  ...props
}: HeatmapFetcherProps) => {
  const { t } = useTranslation();
  const { dateRange } = useServerMapSearchParameters();
  const [setting] = useStoragedSetting(APP_SETTING_KEYS.HEATMAP_SETTING);

  const parameters: GetHeatmapAppData.Parameters = {
    applicationName: nodeData?.applicationName,
    serviceTypeName: nodeData?.serviceType,
    from: toBasicISOString(dateRange.from),
    to: toBasicISOString(dateRange.to),
    minElapsedTime: Number(setting?.yMin) || DefaultAxisY[0],
    maxElapsedTime: Number(setting?.yMax) || DefaultAxisY[1],
    agentId: agentId,
  };
  const { data, isLoading, error } = useGetHeatmapAppData(parameters, serviceName);

  return (
    <div className="relative w-full h-full">
      {error && (
        <div className="absolute left-0 right-0 z-1000 flex items-center justify-center top-[20px] bottom-[100px]">
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
        serviceName={serviceName}
        {...props}
      />
    </div>
  );
};
