import React from 'react';
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

  const [parameters, setParameters] = React.useState<GetHeatmapAppData.Parameters>({
    applicationName: nodeData?.applicationName,
    serviceTypeName: nodeData?.serviceType,
    from: toBasicISOString(dateRange.from),
    to: toBasicISOString(dateRange.to),
    minElapsedTime: Number(setting?.yMin) || DefaultAxisY[0],
    maxElapsedTime: Number(setting?.yMax) || DefaultAxisY[1],
    agentId: agentId,
  });
  // 조회 파라미터가 effect로 한 박자 늦게 따라오므로 serviceName도 같은 effect에서 함께 갱신한다.
  // 그러지 않으면 대상이 바뀌는 렌더에서 (이전 application, 새 service) 짝의 queryKey가 한 번
  // 만들어져, 그 service에 없는 application을 조회하는 요청이 나간다.
  // (아래 HeatmapChartCore에 넘기는 값은 링크 생성용이라 최신값 그대로 쓴다.)
  const [requestServiceName, setRequestServiceName] = React.useState(serviceName);
  const { data, isLoading, error } = useGetHeatmapAppData(parameters, requestServiceName);

  React.useEffect(() => {
    setParameters({
      applicationName: nodeData?.applicationName,
      serviceTypeName: nodeData?.serviceType,
      from: toBasicISOString(dateRange.from),
      to: toBasicISOString(dateRange.to),
      minElapsedTime: Number(setting?.yMin) || DefaultAxisY[0],
      maxElapsedTime: Number(setting?.yMax) || DefaultAxisY[1],
      agentId: agentId,
    });
    setRequestServiceName(serviceName);
  }, [
    dateRange.from.getTime(),
    dateRange.to.getTime(),
    setting?.yMin,
    setting?.yMax,
    nodeData?.applicationName,
    nodeData?.serviceType,
    agentId,
    serviceName,
  ]);

  return (
    <div className="relative w-full h-full">
      {error && (
        <div className="absolute left-0 right-0 z-[1000] flex items-center justify-center top-[20px] bottom-[100px]">
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
