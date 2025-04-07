import React from 'react';
import {
  ErrorBoundary,
  ChartSkeleton,
  GetServerMap,
  ApdexScore,
  useStoragedAxisY,
  APP_SETTING_KEYS,
  BASE_PATH,
  getScatterFullScreenPath,
  convertParamsToQueryString,
  useServerMapSearchParameters,
  HelpPopover,
  getHeatmapFullScreenPath,
  HeatmapFetcher,
  HeatmapFetcherHandle,
  cn,
  ApplicationType,
} from '@pinpoint-fe/ui';
import { FaDownload, FaExpandArrowsAlt } from 'react-icons/fa';
import { BsGearFill } from 'react-icons/bs';
import { CgSpinner } from 'react-icons/cg';
import { HeatmapSetting } from './HeatmapSetting';
export interface ScatterOrHeatmapProps {
  chartType?: 'scatter' | 'heatmap';
  realtime?: boolean;
  agentId?: string;
  application?: ApplicationType;
  nodeData?: GetServerMap.NodeData;
  toolbarOption?: any;
}

export const ScatterOrHeatmap = ({
  realtime = false,
  agentId,
  chartType = 'scatter',
  application,
  nodeData,
  toolbarOption,
}: ScatterOrHeatmapProps) => {
  const chartRef = React.useRef<HeatmapFetcherHandle>();
  const { dateRange, searchParameters } = useServerMapSearchParameters();

  const showApdex = React.useMemo(() => {
    return !nodeData && chartType === 'scatter';
  }, [nodeData, chartType]);

  const [showSetting, setShowSetting] = React.useState(false);
  const [isCapturingImage, setIsCapturingImage] = React.useState(false);

  const [y, setY] = useStoragedAxisY(
    chartType === 'scatter'
      ? APP_SETTING_KEYS.SCATTER_Y_AXIS_MIN_MAX
      : APP_SETTING_KEYS.HEATMAP_Y_AXIS_MIN_MAX,
    [0, 10000],
  );

  const handleExpand = () => {
    if (realtime) {
      return;
    }
    window.open(
      `${BASE_PATH}${
        chartType === 'scatter'
          ? getScatterFullScreenPath(application)
          : getHeatmapFullScreenPath(application)
      }?${convertParamsToQueryString({
        from: searchParameters.from,
        to: searchParameters.to,
      })}`,
      '_blank',
    );
  };

  const handleCaptureImage = async () => {
    if (!chartRef.current) {
      return;
    }

    await setIsCapturingImage(true);
    chartRef.current?.handleCaptureImage?.();
    await setIsCapturingImage(false);
  };

  const handleSettingApply = (newSetting: { yMin: number; yMax: number }) => {
    setY([newSetting.yMin, newSetting.yMax]);
  };

  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ChartSkeleton />}>
        <div className="relative w-full h-full">
          <div
            className={cn('flex flex-row', {
              'justify-between': showApdex,
              'justify-end': !showApdex,
            })}
          >
            {showApdex && <ApdexScore nodeData={nodeData} />}
            <div className="flex flex-row items-center justify-end gap-2 px-4 font-normal text-gray-400">
              <BsGearFill
                className="text-base cursor-pointer"
                onClick={() => setShowSetting(true)}
              />
              <FaDownload className="text-base cursor-pointer" onClick={handleCaptureImage} />
              {!toolbarOption?.expand?.hide && (
                <FaExpandArrowsAlt className="text-base cursor-pointer" onClick={handleExpand} />
              )}
              {chartType === 'scatter' && <HelpPopover helpKey="HELP_VIEWER.SCATTER" />}
            </div>
          </div>
          {chartType === 'scatter' ? (
            <div className="w-full p-5 mb-12 aspect-[1.618]">
              {/* {realtime ? (
                <ScatterChartRealtimeFetcher node={node} />
              ) : staticMode ? (
                <ScatterChartStatic
                  ref={chartRef}
                  application={serverMapCurrentTarget!}
                  selectedAgentId={agentId}
                />
              ) : (
                <ScatterChartFetcher
                  ref={chartRef}
                  node={serverMapCurrentTarget}
                  agentId={agentId}
                  y={y || [0, 10000]}
                />
              )} */}
            </div>
          ) : (
            <HeatmapFetcher ref={chartRef} application={application} agentId={agentId} />
          )}
          {(showSetting || isCapturingImage) && (
            <div className="absolute inset-0 z-[1000] flex items-center justify-center">
              <div className="absolute inset-0 opacity-50 bg-background"></div>
              {showSetting && (
                <HeatmapSetting
                  className="z-10"
                  defaultValues={{
                    yMin: y[0],
                    yMax: y[1],
                  }}
                  onClose={() => setShowSetting(false)}
                  onApply={(newSetting) => {
                    handleSettingApply(newSetting);
                  }}
                />
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
      </React.Suspense>
    </ErrorBoundary>
  );
};
