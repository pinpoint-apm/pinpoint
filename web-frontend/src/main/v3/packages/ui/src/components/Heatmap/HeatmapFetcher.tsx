import React from 'react';
import { BsGearFill } from 'react-icons/bs';
import { DefaultValue, HeatmapSetting } from './HeatmapSetting';
import HeatmapChart from './HeatmapChart';
import { useStoragedAxisY } from '@pinpoint-fe/ui/src/hooks';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';
import { FaDownload, FaExpandArrowsAlt } from 'react-icons/fa';
import { CgSpinner } from 'react-icons/cg';
import ReactEChartsCore from 'echarts-for-react/lib/core';
import html2canvas from 'html2canvas';

export const HeatmapFetcher = () => {
  const chartRef = React.useRef<ReactEChartsCore>(null);
  const [y, setY] = useStoragedAxisY(APP_SETTING_KEYS.HEATMAP_Y_AXIS_MIN_MAX, [
    DefaultValue.yMin,
    DefaultValue.yMax,
  ]);
  const [showSetting, setShowSetting] = React.useState(false);
  const [isCapturingImage, setIsCapturingImage] = React.useState(false);

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

  return (
    <div className="relative w-full h-full">
      <div className="flex flex-row items-center justify-end h-full gap-2 px-4 font-normal text-gray-400">
        <FaExpandArrowsAlt
          className="text-base cursor-pointer"
          onClick={() => console.log('outlink')}
        />
        <FaDownload className="text-base cursor-pointer" onClick={handleCaptureImage} />
        <BsGearFill className="text-base cursor-pointer" onClick={() => setShowSetting(true)} />
      </div>
      <HeatmapChart
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
