import React from 'react';
import html2canvas from 'html2canvas';
import {
  APP_SETTING_KEYS,
  BASE_PATH,
  colors,
  GetHeatmapAppData,
  GetServerMap,
  ApplicationType,
} from '@pinpoint-fe/ui/src/constants';
import HeatmapChart from './HeatmapChart';
import {
  HelpPopover,
  cn,
  convertParamsToQueryString,
  getFormattedDateRange,
  getHeatmapFullScreenPath,
  getHeatmapFullScreenRealtimePath,
  getScatterFullScreenRealtimePath,
  getTransactionListPath,
  getTranscationListQueryString,
  useServerMapSearchParameters,
  useStoragedSetting,
} from '@pinpoint-fe/ui';
import { FaDownload, FaExpandArrowsAlt } from 'react-icons/fa';
import { BsGearFill } from 'react-icons/bs';
import { CgSpinner } from 'react-icons/cg';
import { HeatmapSetting, HeatmapSettingType } from './HeatmapSetting';
import { HeatmapSkeleton } from '../HeatmapSkeleton';

const colorSteps = 10;
export const HeatmapColor = {
  success: Array.from({ length: colorSteps }, (_, i) => {
    return colors.green[(i + 1) * 100 === 1000 ? 950 : (i + 1) * 100];
  }),
  fail: Array.from({ length: colorSteps }, (_, i) => {
    return colors.red[(i + 1) * 100 === 1000 ? 950 : (i + 1) * 100];
  }),
  selected: colors.yellow[200],
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
};

const HeatmapChartCore = ({
  isRealtime,
  isLoading,
  nodeData,
  data,
  agentId,
  toolbarOption,
}: HeatmapChartCoreProps) => {
  const chartContainerRef = React.useRef<HTMLDivElement>(null);

  const { dateRange, searchParameters, application } = useServerMapSearchParameters();
  const [showSetting, setShowSetting] = React.useState(false);
  const [isCapturingImage, setIsCapturingImage] = React.useState(false);

  const [setting, setSetting] = useStoragedSetting(APP_SETTING_KEYS.HEATMAP_SETTING);

  const handleExpand = () => {
    if (isRealtime) {
      window.open(
        `${BASE_PATH}${getHeatmapFullScreenRealtimePath(nodeData)}?${convertParamsToQueryString({
          agentId,
        })}`,
        '_blank',
      );
      return;
    }
    window.open(
      `${BASE_PATH}${getHeatmapFullScreenPath(nodeData)}?${convertParamsToQueryString({
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
    const fileName = `Pinpoint_Heatmap_Chart__${(agentId ? agentId : currentNode) || ''}`;

    const canvas = await html2canvas(chartContainerRef.current);
    const image = canvas.toDataURL('image/png');

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

  const onDragEnd = (data: any, checkedLegends: any) => {
    window.open(
      `${BASE_PATH}${getTransactionListPath(
        application,
        isRealtime ? getFormattedDateRange(dateRange) : searchParameters,
      )}&${getTranscationListQueryString({
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
      <div className={cn('flex flex-row justify-end')}>
        <div className="flex flex-row items-center justify-end gap-2 px-4 font-normal text-gray-400">
          <BsGearFill className="text-base cursor-pointer" onClick={() => setShowSetting(true)} />
          <FaDownload className="text-base cursor-pointer" onClick={handleCaptureImage} />
          {!toolbarOption?.expand?.hide && (
            <FaExpandArrowsAlt className="text-base cursor-pointer" onClick={handleExpand} />
          )}
          {/* <HelpPopover helpKey="HELP_VIEWER.SCATTER" /> */}
        </div>
      </div>
      <HeatmapChart
        ref={chartContainerRef}
        data={data}
        isRealtime={isRealtime}
        setting={setting}
        onDragEnd={onDragEnd}
      />
      {(showSetting || isCapturingImage) && (
        <div className="absolute inset-0 z-[1000] flex items-center justify-center">
          <div className="absolute inset-0 opacity-50 bg-background"></div>
          {showSetting && (
            <HeatmapSetting
              className="z-10"
              isRealtime={isRealtime}
              defaultValues={setting}
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
  );
};

export default HeatmapChartCore;
