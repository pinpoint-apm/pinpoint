import React from 'react';
import ReactEChartsCore from 'echarts-for-react/lib/core';
import * as echarts from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { HeatmapChart as HeatmapChartEcharts } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  VisualMapComponent,
  GraphicComponent,
} from 'echarts/components';
import { ECharts, EChartsOption } from 'echarts';
import { mockData } from './mockData';
import { colors } from '@pinpoint-fe/ui/src/constants';
import { capitalize, set } from 'lodash';
import { defaultTickFormatter } from '@pinpoint-fe/ui/src/components/ReChart';
import { HeatmapSettingType } from './HeatmapSetting';

echarts.use([
  CanvasRenderer, // 캔버스 렌더링
  HeatmapChartEcharts, // 히트맵 차트
  GridComponent, // 그리드
  TooltipComponent, // 툴팁
  VisualMapComponent, // 색상 범례
  GraphicComponent, // 그래픽
]);

echarts.use([HeatmapChartEcharts, CanvasRenderer]);

export const HeatmapColor = {
  success: '#34b994',
  failed: '#eb4748',
};

type HeatmapChartCoreProps = {
  setting: HeatmapSettingType;
};

const HeatmapChartCore = React.forwardRef(
  ({ setting }: HeatmapChartCoreProps, ref: React.Ref<ReactEChartsCore>) => {
    const containerRef = React.useRef<HTMLDivElement>(null);
    const [containerSize, setContainerSize] = React.useState({
      width: 0,
      height: 0,
    });

    const successData: [string, string, number][] = [];
    const failedData: [string, string, number][] = [];
    let maxFailedCount = 0;
    let maxSuccessCount = 0;

    React.useEffect(() => {
      const wrapperElement = containerRef.current;
      if (!wrapperElement) return;
      const resizeObserver = new ResizeObserver(() => {
        setContainerSize({
          width: wrapperElement.clientWidth,
          height: wrapperElement.clientHeight,
        });
      });
      resizeObserver.observe(wrapperElement);

      return () => {
        resizeObserver.disconnect();
      };
    }, []);

    const { matrixData } = mockData;
    matrixData.forEach((row) => {
      row.cellData.forEach((cell) => {
        successData.push([String(row.timestamp), String(cell.elapsedTime), cell.successCount]);
        failedData.push([String(row.timestamp), String(cell.elapsedTime), cell.failCount]);

        maxSuccessCount = Math.max(maxSuccessCount, cell.successCount);
        maxFailedCount = Math.max(maxFailedCount, cell.failCount);
      });
    });

    const totalSuccessCount = setting.yMax;
    const totalFailedCount = setting.yMax;

    const xAxisData = matrixData.map((row) => String(row.timestamp));
    const yAxisData = matrixData[0].cellData.map((cell) => String(cell.elapsedTime));

    // console.log('successData', successData);
    // console.log('failedData', failedData);

    const option: EChartsOption = {
      tooltip: {
        borderColor: colors.gray[300],
        textStyle: {
          fontFamily: 'inherit',
          fontSize: 8,
        },
        formatter: (params: any) => {
          const { data } = params;
          const [timestamp, elapsedTime, failedCount] = data;
          const date = new Date(timestamp);
          const successCount =
            successData.find(
              (item: [string, string, number]) => item[0] === timestamp && item[1] === elapsedTime,
            )?.[2] || 'N/A';

          return `
        <div style="display: flex; flex-direction: column; gap: 5px; padding: 2px;">
          <div style="margin-bottom: 5px;"><strong>${defaultTickFormatter(date.getTime())}</strong></div>
          ${['success', 'failed']
            .map((type) => {
              const count = type === 'success' ? successCount : failedCount;
              const color = type === 'success' ? HeatmapColor.success : HeatmapColor.failed;

              return `
              <div style="display: flex; justify-content: space-between; gap: 5px;">
                <div style="display: flex; gap: 6px; align-items: center;">
                  <div style="width: 8px; height: 8px; background: ${color}"></div>${capitalize(type)}
                </div>
                <div>${Number(count).toLocaleString()}</div>
              </div>
            `;
            })
            .join('')}
        </div>
        `;
        },
      },
      grid: {
        left: setting.yMax.toString().length * 10,
        right: '10px',
        top: '2%',
        bottom: '20%',
      },
      xAxis: {
        type: 'category',
        data: xAxisData.sort((a, b) => new Date(a).getTime() - new Date(b).getTime()),
        axisLabel: {
          interval: 'auto',
          showMaxLabel: true,
          showMinLabel: true,
          formatter: (value: string) => {
            const date = new Date(value);
            return defaultTickFormatter(date.getTime());
          },
        },
      },
      yAxis: {
        type: 'category',
        data: yAxisData.filter(
          (yValue) => Number(yValue) >= setting.yMin && Number(yValue) <= setting.yMax,
        ),
        axisLabel: {
          interval: (index: number, value: string) => {
            if (yAxisData.length <= 5) {
              return true;
            }

            const step = (yAxisData.length - 1) / 4;
            const positions = [
              0,
              ...Array.from({ length: 3 }, (_, i) => Math.round((i + 1) * step)),
              yAxisData.length - 1,
            ];

            return positions.includes(index);
          },
          formatter: (value: string) => {
            try {
              return Number(value).toLocaleString();
            } catch (err) {
              return value;
            }
          },
        },
      },
      visualMap: [
        {
          min: 0,
          max: maxSuccessCount,
          calculable: true,
          seriesIndex: 0,
          orient: 'horizontal',
          itemHeight: (containerSize.width || 100) * 0.3,
          right: '45%',
          bottom: '4%',
          hoverLink: false,
          formatter: (value) => {
            if (value === setting.yMax) {
              return '';
            }
            return Math.floor(Number(value)).toLocaleString();
          },
          inRange: {
            color: ['#ffffff', HeatmapColor.success],
          },
        },
        {
          min: 0,
          max: maxFailedCount,
          calculable: true,
          seriesIndex: 1,
          orient: 'horizontal',
          itemHeight: (containerSize.width || 100) * 0.3,
          left: '55%',
          bottom: '4%',
          hoverLink: false,
          formatter: (value) => {
            return Math.floor(Number(value)).toLocaleString();
          },
          inRange: {
            color: ['#ffffff', HeatmapColor.failed],
          },
        },
      ],
      graphic: [
        {
          type: 'text',
          bottom: '0%',
          left: 'center',
          style: {
            text: `Success {boldSuccess|${Math.floor(totalSuccessCount).toLocaleString()}}    Failed {boldFailed|${Math.floor(totalFailedCount).toLocaleString()}}`,
            fontSize: 15,
            fill: colors.gray[500],
            rich: {
              boldSuccess: {
                fontWeight: 'bold',
                fill: HeatmapColor.success,
              },
              boldFailed: {
                fontWeight: 'bold',
                fill: HeatmapColor.failed,
              },
            },
          },
        },
      ],
      series: [
        {
          name: 'success',
          type: 'heatmap',
          data: successData,
        },
        {
          name: 'failed',
          type: 'heatmap',
          data: failedData,
          itemStyle: {
            opacity: 0.5,
          },
          emphasis: {
            itemStyle: {
              borderColor: '#333',
              borderWidth: 1,
            },
          },
        },
      ],
    };

    return (
      <div ref={containerRef} className="relative w-full h-full">
        <ReactEChartsCore
          ref={ref}
          echarts={echarts}
          option={option}
          style={{ height: '100%', width: '100%', minHeight: 500 }}
        />
      </div>
    );
  },
);

export default HeatmapChartCore;
