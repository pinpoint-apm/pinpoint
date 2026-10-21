import React from 'react';
import { ErrorBoundary as ReactErrorBoundary } from 'react-error-boundary';
import { lazyWithPreload } from '@pinpoint-fe/ui/src/utils/lazyWithPreload';
import { toPng } from 'html-to-image';
import { GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';
import HeatmapChart, { HeatmapChartProps } from './HeatmapChart';
import { HelpPopover } from '../../HelpPopover/HelpPopover';
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

// ScatterChartCore 의 ScatterToolbarOption 과 같은 모양. 버튼마다 숨길 수 있고, hide 는 툴바 전체를 숨긴다.
export type HeatmapToolbarOption = {
  axisSetting?: { hide?: boolean };
  captureImage?: { hide?: boolean };
  expand?: { hide?: boolean };
  help?: { hide?: boolean };
  hide?: boolean;
};

export interface HeatmapChartCoreProps {
  isRealtime?: boolean;
  isLoading?: boolean;
  data?: GetHeatmapAppData.Response;
  setting: HeatmapSettingType;
  onSettingChange: (setting: HeatmapSettingType) => void;
  onDragEnd?: HeatmapChartProps['onDragEnd'];
  /**
   * 넘기지 않으면 확대 버튼을 숨긴다.
   */
  onExpand?: () => void;
  captureFileName: string;
  toolbarOption?: HeatmapToolbarOption;
}

// 툴바(설정·다운로드·확대)와 차트를 받은 값과 콜백만으로 그린다. 조회·URL·저장소를 모르므로
// 웹은 HeatmapFetcher·HeatmapRealtimeFetcher 가, MCP Apps 뷰는 도구 결과와 호스트 동작으로 채워 넘긴다.
export const HeatmapChartCore = ({
  isRealtime,
  isLoading,
  data,
  setting,
  onSettingChange,
  onDragEnd,
  onExpand,
  captureFileName,
  toolbarOption,
}: HeatmapChartCoreProps) => {
  const chartContainerRef = React.useRef<HTMLDivElement>(null);
  const [showSetting, setShowSetting] = React.useState(false);
  const [isCapturingImage, setIsCapturingImage] = React.useState(false);

  const handleCaptureImage = async () => {
    if (!chartContainerRef.current) {
      return;
    }

    await setIsCapturingImage(true);

    const image = await toPng(chartContainerRef.current, {
      backgroundColor: chartContainerRef.current.style.backgroundColor || '#ffffff',
      cacheBust: true,
    });

    const link = document.createElement('a');
    link.href = image;
    link.download = captureFileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    await setIsCapturingImage(false);
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
      {!toolbarOption?.hide && (
        <div className="absolute flex flex-row items-center justify-end gap-2 px-4 font-normal text-gray-400 -top-5 right-3">
          {!toolbarOption?.axisSetting?.hide && (
            <BsGearFill
              aria-label="Heatmap settings"
              className="text-base cursor-pointer"
              onClick={() => setShowSetting(true)}
              onMouseEnter={prefetchHeatmapSetting}
            />
          )}
          {!toolbarOption?.captureImage?.hide && (
            <FaDownload
              aria-label="Download heatmap image"
              className="text-base cursor-pointer"
              onClick={handleCaptureImage}
            />
          )}
          {onExpand && !toolbarOption?.expand?.hide && (
            <FaExpandArrowsAlt
              aria-label="Expand heatmap"
              className="text-base cursor-pointer"
              onClick={onExpand}
            />
          )}
          {!toolbarOption?.help?.hide && <HelpPopover helpKey="HELP_VIEWER.HEATMAP" />}
        </div>
      )}
      <HeatmapChart
        ref={chartContainerRef}
        data={data}
        isRealtime={isRealtime}
        setting={setting}
        onDragEnd={onDragEnd}
      />
      {(showSetting || isCapturingImage) && (
        <div className="absolute inset-0 z-chart-overlay flex items-center justify-center">
          <div className="absolute inset-0 opacity-50 bg-background"></div>
          {showSetting && (
            <ReactErrorBoundary fallback={null} onError={() => setShowSetting(false)}>
              <React.Suspense fallback={null}>
                <HeatmapSetting
                  className="z-10"
                  isRealtime={isRealtime}
                  defaultValues={setting}
                  onClose={() => setShowSetting(false)}
                  onApply={onSettingChange}
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
