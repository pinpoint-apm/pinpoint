import React from 'react';
import { ErrorBoundary as ReactErrorBoundary } from 'react-error-boundary';
import { lazyWithPreload } from '@pinpoint-fe/ui/src/utils/lazyWithPreload';
import { toPng } from 'html-to-image';
import {
  APP_SETTING_KEYS,
  BASE_PATH,
  GetHeatmapAppData,
  GetServerMap,
  ApplicationType,
} from '@pinpoint-fe/ui/src/constants';
import HeatmapChart from './HeatmapChart';
import {
  HelpPopover,
  convertParamsToQueryString,
  getFormattedDateRange,
  getHeatmapFullScreenPath,
  getHeatmapFullScreenRealtimePath,
  getTransactionListPath,
  getTransactionListQueryString,
  useServerMapSearchParameters,
  useRequestService,
  useStoragedSetting,
} from '@pinpoint-fe/ui';
import { FaDownload, FaExpandArrowsAlt } from 'react-icons/fa';
import { BsGearFill } from 'react-icons/bs';
import { CgSpinner } from 'react-icons/cg';
import type { HeatmapSettingType } from './HeatmapSetting';
import { HeatmapSkeleton } from '../HeatmapSkeleton';

// 설정 팝업은 눌러야 열리므로 zod·react-hook-form 과 함께 필요할 때 불러온다.
// 배포로 청크 이름이 바뀐 뒤 예전 탭에서 열면 불러오기가 실패하므로, 렌더하는 곳에서 ErrorBoundary 로
// 팝업만 닫는다(페이지 전체가 에러 화면이 되지 않도록).
const { Component: HeatmapSetting, preload: preloadHeatmapSetting } = lazyWithPreload(() =>
  import('./HeatmapSetting').then((module) => module.HeatmapSetting),
);
// 톱니바퀴에 마우스를 올리면 미리 받아 두어, 처음 열 때 팝업이 비는 순간을 없앤다.
// 미리 받기의 실패는 여기서 무시한다 — 실제로 열 때 다시 불러오고, 그때의 실패는 ErrorBoundary 가 처리한다.
const prefetchHeatmapSetting = () => {
  preloadHeatmapSetting().catch(() => {});
};

export type HeatmapChartCoreProps = {
  isRealtime?: boolean;
  isLoading?: boolean;
  nodeData: GetServerMap.NodeData | ApplicationType;
  data?: GetHeatmapAppData.Response;
  agentId?: string;
  toolbarOption?: {
    expand?: {
      hide?: boolean;
    };
  };
  /**
   * 조회 대상이 소속된 service. 화면의 service와 다를 때만 넘긴다(map에서 다른 service의 노드를
   * 고른 경우). 넘기지 않으면 화면의 service로 조회한다.
   */
  serviceName?: string;
};

const HeatmapChartCore = ({
  isRealtime,
  isLoading,
  nodeData,
  data,
  agentId,
  toolbarOption,
  serviceName,
}: HeatmapChartCoreProps) => {
  const chartContainerRef = React.useRef<HTMLDivElement>(null);

  const { dateRange, searchParameters } = useServerMapSearchParameters();
  // 넘어온 값이 없으면 화면의 service로 조회한다(map 밖에서 쓰이는 경우).
  const screenServiceName = useRequestService();
  const requestServiceName = serviceName ?? screenServiceName;
  const [showSetting, setShowSetting] = React.useState(false);
  const [isCapturingImage, setIsCapturingImage] = React.useState(false);

  const [setting, setSetting] = useStoragedSetting(APP_SETTING_KEYS.HEATMAP_SETTING);

  const handleExpand = () => {
    if (isRealtime) {
      window.open(
        `${BASE_PATH}${getHeatmapFullScreenRealtimePath(
          nodeData,
          undefined,
          requestServiceName,
        )}?${convertParamsToQueryString({
          agentId,
        })}`,
        '_blank',
      );
      return;
    }
    window.open(
      `${BASE_PATH}${getHeatmapFullScreenPath(
        nodeData,
        undefined,
        requestServiceName,
      )}?${convertParamsToQueryString({
        from: searchParameters.from,
        to: searchParameters.to,
      })}`,
      '_blank',
    );
  };

  const handleCaptureImage = async () => {
    if (!chartContainerRef.current) {
      return;
    }

    await setIsCapturingImage(true);

    const currentNode = `${nodeData?.applicationName}^${nodeData?.serviceType}`;
    const fileName = `Pinpoint_Heatmap_Chart__${(agentId ? agentId : currentNode) || ''}.png`;

    const image = await toPng(chartContainerRef.current, {
      backgroundColor: chartContainerRef.current.style.backgroundColor || '#ffffff',
      cacheBust: true,
    });

    const link = document.createElement('a');
    link.href = image;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    await setIsCapturingImage(false);
  };

  const handleSettingApply = (newSetting: HeatmapSettingType) => {
    setSetting(newSetting);
  };

  const onDragEnd = (
    data: { x1: number; y1: number; x2: number; y2: number },
    checkedLegends: string[],
  ) => {
    window.open(
      `${BASE_PATH}${getTransactionListPath(
        nodeData,
        isRealtime ? getFormattedDateRange(dateRange) : searchParameters,
        requestServiceName,
      )}&${getTransactionListQueryString({
        ...data,
        checkedLegends,
        agentId,
      })}`,
    );
  };

  if (isLoading) {
    return (
      <div className="relative flex flex-col w-full h-full gap-4">
        <HeatmapSkeleton />
      </div>
    );
  }

  return (
    <div className="relative flex flex-col w-full h-full gap-2">
      <div className="absolute flex flex-row items-center justify-end gap-2 px-4 font-normal text-gray-400 -top-5 right-3">
        <BsGearFill
          className="text-base cursor-pointer"
          onClick={() => setShowSetting(true)}
          onMouseEnter={prefetchHeatmapSetting}
        />
        <FaDownload className="text-base cursor-pointer" onClick={handleCaptureImage} />
        {!toolbarOption?.expand?.hide && (
          <FaExpandArrowsAlt className="text-base cursor-pointer" onClick={handleExpand} />
        )}
        <HelpPopover helpKey="HELP_VIEWER.HEATMAP" />
      </div>
      <HeatmapChart
        ref={chartContainerRef}
        data={data}
        isRealtime={isRealtime}
        setting={setting}
        onDragEnd={onDragEnd}
      />
      {(showSetting || isCapturingImage) && (
        <div className="absolute inset-0 z-1000 flex items-center justify-center">
          <div className="absolute inset-0 opacity-50 bg-background"></div>
          {showSetting && (
            <ReactErrorBoundary fallback={null} onError={() => setShowSetting(false)}>
              <React.Suspense fallback={null}>
                <HeatmapSetting
                  className="z-10"
                  isRealtime={isRealtime}
                  defaultValues={setting}
                  onClose={() => setShowSetting(false)}
                  onApply={(newSetting) => {
                    handleSettingApply(newSetting);
                  }}
                />
              </React.Suspense>
            </ReactErrorBoundary>
          )}
          {isCapturingImage && (
            <>
              <div className="z-10">Image capturing...</div>
              <CgSpinner className="animate-spin" />
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default HeatmapChartCore;
