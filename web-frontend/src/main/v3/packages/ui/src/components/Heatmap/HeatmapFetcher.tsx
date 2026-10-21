import {
  HeatmapChartCore,
  HeatmapChartCoreProps,
  HeatmapToolbarOption,
} from './core/HeatmapChartCore';
import {
  useServerMapSearchParameters,
  useStoragedSetting,
  useGetHeatmapAppData,
  useRequestService,
} from '@pinpoint-fe/ui/src/hooks';
import {
  APP_SETTING_KEYS,
  ApplicationType,
  BASE_PATH,
  GetHeatmapAppData,
  GetServerMap,
} from '@pinpoint-fe/ui/src/constants';
import {
  convertParamsToQueryString,
  getHeatmapFullScreenPath,
  getTransactionListPath,
  getTransactionListQueryString,
  toBasicISOString,
} from '@pinpoint-fe/ui/src/utils';
import { useTranslation } from 'react-i18next';

const DefaultAxisY = [0, 10000];

export interface HeatmapFetcherProps {
  nodeData: GetServerMap.NodeData | ApplicationType;
  agentId?: string;
  toolbarOption?: HeatmapToolbarOption;
  /**
   * 조회 대상이 소속된 service. 화면의 service와 다를 때만 넘긴다(map에서 다른 service의 노드를
   * 고른 경우). 넘기지 않으면 화면의 service로 조회한다.
   */
  serviceName?: string;
}

// 조회한 데이터를 HeatmapChartCore 로 그린다. 설정은 저장소에, 확대·드래그는 URL로 새 창을 연다.
export const HeatmapFetcher = ({
  nodeData,
  agentId,
  toolbarOption,
  serviceName,
}: HeatmapFetcherProps) => {
  const { t } = useTranslation();
  const { dateRange, searchParameters } = useServerMapSearchParameters();
  const [setting, setSetting] = useStoragedSetting(APP_SETTING_KEYS.HEATMAP_SETTING);
  // 넘어온 값이 없으면 화면의 service로 링크를 만든다(map 밖에서 쓰이는 경우).
  const screenServiceName = useRequestService();
  const linkServiceName = serviceName ?? screenServiceName;

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

  const handleExpand = () => {
    window.open(
      `${BASE_PATH}${getHeatmapFullScreenPath(
        nodeData,
        undefined,
        linkServiceName,
      )}?${convertParamsToQueryString({
        from: searchParameters.from,
        to: searchParameters.to,
      })}`,
      '_blank',
    );
  };

  const handleDragEnd: HeatmapChartCoreProps['onDragEnd'] = (area, checkedLegends) => {
    window.open(
      `${BASE_PATH}${getTransactionListPath(
        nodeData,
        searchParameters,
        linkServiceName,
      )}&${getTransactionListQueryString({
        ...area,
        checkedLegends,
      })}`,
    );
  };

  return (
    <div className="relative w-full h-full">
      {error && (
        <div className="absolute left-0 right-0 z-chart-overlay flex items-center justify-center top-[20px] bottom-[100px]">
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
        setting={setting}
        onSettingChange={setSetting}
        onDragEnd={handleDragEnd}
        onExpand={handleExpand}
        captureFileName={`Pinpoint_Heatmap_Chart__${nodeData?.applicationName}^${nodeData?.serviceType}.png`}
        toolbarOption={toolbarOption}
      />
    </div>
  );
};
