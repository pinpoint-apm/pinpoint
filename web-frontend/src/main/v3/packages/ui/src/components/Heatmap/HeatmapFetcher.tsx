import React from 'react';
import HeatmapChartCore from './HeatmapChartCore';
import ReactEChartsCore from 'echarts-for-react/lib/core';
import html2canvas from 'html2canvas';
import {
  useServerMapSearchParameters,
  useStoragedAxisY,
  useGetHeatmapAppData,
} from '@pinpoint-fe/ui/src/hooks';
import {
  APP_SETTING_KEYS,
  ApplicationType,
  GetHeatmapAppData,
} from '@pinpoint-fe/ui/src/constants';

export interface HeatmapFetcherHandle {
  handleCaptureImage: () => Promise<void>;
}

export type HeatmapFetcherProps = {
  application?: ApplicationType;
  agentId?: string;
};

export const HeatmapFetcher = React.forwardRef(
  ({ application, agentId }: HeatmapFetcherProps, ref) => {
    React.useImperativeHandle(ref, () => ({
      handleCaptureImage,
    }));

    const { dateRange, searchParameters } = useServerMapSearchParameters();
    const chartRef = React.useRef<ReactEChartsCore>(null);
    const [y] = useStoragedAxisY(APP_SETTING_KEYS.HEATMAP_Y_AXIS_MIN_MAX, [0, 10000]);
    const [parameters, setParameters] = React.useState<GetHeatmapAppData.Parameters>({
      applicationName: application?.applicationName,
      from: dateRange.from.getTime(),
      to: dateRange.to.getTime(),
      minElapsedTime: Number(y[0]),
      maxElapsedTime: Number(y[1]),
      agentId: agentId,
    });
    const { data, isLoading } = useGetHeatmapAppData(parameters);

    // console.log('search', searchParameters, 'parameters', parameters);
    // console.log('data', data);

    React.useEffect(() => {
      setParameters({
        applicationName: application?.applicationName,
        from: dateRange.from.getTime(),
        to: dateRange.to.getTime(),
        minElapsedTime: Number(y[0]),
        maxElapsedTime: Number(y[1]),
        agentId: agentId,
      });
    }, [
      dateRange.from.getTime(),
      dateRange.to.getTime(),
      y[0],
      y[1],
      application?.applicationName,
      agentId,
    ]);

    async function handleCaptureImage() {
      if (!chartRef.current) {
        return;
      }

      const currentNode = '';
      const fileName = `Pinpoint_Heatmap_Chart__${(agentId ? agentId : currentNode) || ''}`;

      const chartElement = chartRef.current.getEchartsInstance().getDom();
      const canvas = await html2canvas(chartElement);
      const image = canvas.toDataURL('image/png');

      const link = document.createElement('a');
      link.href = image;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }

    return (
      <div className="relative w-full h-full">
        <HeatmapChartCore
          ref={chartRef}
          isLoading={isLoading}
          data={data}
          setting={{
            yMin: y[0],
            yMax: y[1],
          }}
        />
      </div>
    );
  },
);
