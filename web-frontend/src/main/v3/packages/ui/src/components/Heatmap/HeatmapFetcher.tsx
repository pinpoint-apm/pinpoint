import React from 'react';
import { BsGearFill } from 'react-icons/bs';
import { DefaultValue, HeatmapSetting } from './HeatmapSetting';
import HeatmapChartCore from './HeatmapChartCore';
import { useStoragedAxisY } from '@pinpoint-fe/ui/src/hooks';
import { APP_SETTING_KEYS, BASE_PATH } from '@pinpoint-fe/ui/src/constants';
import { FaDownload, FaExpandArrowsAlt } from 'react-icons/fa';
import { CgSpinner } from 'react-icons/cg';
import ReactEChartsCore from 'echarts-for-react/lib/core';
import html2canvas from 'html2canvas';
import { CurrentTarget } from '@pinpoint-fe/ui/src/atoms';
import { convertParamsToQueryString, getHeatmapFullScreenPath } from '@pinpoint-fe/ui/src/utils';
import { useServerMapSearchParameters } from '@pinpoint-fe/ui/src/hooks';

export type HeatmapFetcherProps = {
  node: CurrentTarget;
  agentId?: string;
  toolbarOption?: {
    expand: {
      hide?: boolean;
      onClick?: () => void;
    };
  };
};

export const HeatmapFetcher = ({ node, agentId, toolbarOption }: HeatmapFetcherProps) => {
  const { searchParameters } = useServerMapSearchParameters();
  const chartRef = React.useRef<ReactEChartsCore>(null);
  const [y, setY] = useStoragedAxisY(APP_SETTING_KEYS.HEATMAP_Y_AXIS_MIN_MAX, [
    DefaultValue.yMin,
    DefaultValue.yMax,
  ]);
  const [showSetting, setShowSetting] = React.useState(false);
  const [isCapturingImage, setIsCapturingImage] = React.useState(false);

  console.log('node', node, 'agentId', agentId, 'searchParameters', searchParameters);

  async function handleCaptureImage() {
    if (!chartRef.current) {
      return;
    }

    await setIsCapturingImage(true);

    const currentNode = '';
    const fileName = `Pinpoint_Scatter_Chart__${currentNode || ''}`;

    const chartElement = chartRef.current.getEchartsInstance().getDom();
    const canvas = await html2canvas(chartElement);
    const image = canvas.toDataURL('image/png');

    const link = document.createElement('a');
    link.href = image;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    await setIsCapturingImage(false);
  }

  function handleExpand() {
    window.open(
      `${BASE_PATH}${getHeatmapFullScreenPath(node)}?${convertParamsToQueryString({
        from: searchParameters.from,
        to: searchParameters.to,
      })}`,
      '_blank',
    );
  }

  return (
    <div className="relative w-full h-full">
      <div className="flex flex-row items-center justify-end gap-2 px-4 font-normal text-gray-400">
        {!toolbarOption?.expand?.hide && (
          <FaExpandArrowsAlt className="text-base cursor-pointer" onClick={handleExpand} />
        )}
        <FaDownload className="text-base cursor-pointer" onClick={handleCaptureImage} />
        <BsGearFill className="text-base cursor-pointer" onClick={() => setShowSetting(true)} />
      </div>
      <HeatmapChartCore
        ref={chartRef}
        setting={{
          yMin: y[0],
          yMax: y[1],
        }}
      />
      {(showSetting || isCapturingImage) && (
        <div className="absolute inset-0 z-10 flex items-center justify-center">
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
                setY([newSetting.yMin, newSetting.yMax]);
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
  );
};
