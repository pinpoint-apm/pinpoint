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
import { colors, GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';
import { capitalize, debounce } from 'lodash';
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
  selected: 'blue',
};

type HeatmapChartCoreProps = {
  isLoading?: boolean;
  data?: GetHeatmapAppData.Response;
  setting: HeatmapSettingType;
};

const HeatmapChartCore = React.forwardRef(
  ({ data, setting }: HeatmapChartCoreProps, ref: React.Ref<ReactEChartsCore>) => {
    const containerRef = React.useRef<HTMLDivElement>(null);
    const [containerSize, setContainerSize] = React.useState({
      width: 0,
      height: 0,
    });
    const chartRef = React.useRef<ReactEChartsCore>(null);

    const [successRange, setSuccessRange] = React.useState(); // 성공 범위: [시작, 끝]
    const [failRange, setFailRange] = React.useState(); // 성공 범위: [시작, 끝]

    const [startCell, setStartCell] = React.useState(''); // 시작 셀: x-y
    const [endCell, setEndCell] = React.useState(''); // 끝 셀: x-y

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

    const isSelectedCell = React.useCallback(
      (x: number, y: number) => {
        const [startX, startY] = startCell.split('-').map(Number);
        const [endX, endY] = endCell.split('-').map(Number);

        if (!startX || !startY || !endX || !endY) {
          return false;
        }

        const left = Math.min(startX, endX);
        const right = Math.max(startX, endX);
        const top = Math.max(startY, endY);
        const bottom = Math.min(startY, endY);

        return x >= left && x <= right && y <= top && y >= bottom;
      },
      [startCell, endCell],
    );

    const dataForRender = React.useMemo(() => {
      const successData: [string, string, number][] = [];
      const failData: {
        value: [string, string, number];
        itemStyle?: {
          color: string;
          opacity: number;
        };
      }[] = [];
      let maxFailCount = 0;
      let maxSuccessCount = 0;

      const { heatmapData } = data || {};
      heatmapData?.forEach((row) => {
        row?.cellDataList?.forEach((cell) => {
          successData.push([String(row.timestamp), String(cell.elapsedTime), cell.successCount]);
          failData.push({
            value: [String(row.timestamp), String(cell.elapsedTime), cell.failCount],
            itemStyle: isSelectedCell(row.timestamp, cell.elapsedTime)
              ? {
                  color: HeatmapColor.selected,
                  opacity: 0.8,
                }
              : undefined,
          });

          maxSuccessCount = Math.max(maxSuccessCount, cell.successCount);
          maxFailCount = Math.max(maxFailCount, cell.failCount);
        });
      });

      return { successData, failData, maxFailCount, maxSuccessCount };
    }, [data, startCell, endCell]);

    const xAxisData = React.useMemo(() => {
      return data?.heatmapData?.map((row) => String(row.timestamp)) || [];
    }, [data]);
    const yAxisData = React.useMemo(() => {
      return data?.heatmapData?.[0].cellDataList?.map((cell) => String(cell.elapsedTime)) || [];
    }, [data]);

    const totalSuccessCount = React.useMemo(() => {
      return data?.summary?.totalSuccessCount || 0;
    }, [data]);
    const totalFailedCount = React.useMemo(() => {
      return data?.summary?.totalFailCount || 0;
    }, [data]);

    const option: EChartsOption = {
      tooltip: {
        show: !!startCell ? false : true,
        borderColor: colors.gray[300],
        textStyle: {
          fontFamily: 'inherit',
          fontSize: 8,
        },
        formatter: (params: any) => {
          const { data } = params;
          const [timestamp, elapsedTime, failedCount] = data?.value;
          const successCount = dataForRender?.successData.find((item: [string, string, number]) => {
            return item[0] === timestamp && item[1] === elapsedTime;
          })?.[2];
          return `
        <div style="display: flex; flex-direction: column; gap: 5px; padding: 2px;">
          <div style="margin-bottom: 5px;"><strong>${defaultTickFormatter(Number(timestamp))}</strong></div>
          ${['success', 'failed']
            .map((type) => {
              const count = type === 'success' ? successCount : failedCount;
              const color = type === 'success' ? HeatmapColor.success : HeatmapColor.failed;

              return `
              <div style="display: flex; justify-content: space-between; gap: 5px;">
                <div style="display: flex; gap: 6px; align-items: center;">
                  <div style="width: 8px; height: 8px; background: ${color}"></div>${capitalize(type)}
                </div>
                <div>${count === undefined ? 'N/A' : Number(count).toLocaleString()}</div>
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
        bottom: '100px',
      },
      xAxis: {
        type: 'category',
        data: xAxisData.sort((a, b) => Number(a) - Number(b)),
        axisLabel: {
          interval: 'auto',
          showMaxLabel: true,
          showMinLabel: true,
          formatter: (value: string) => {
            return defaultTickFormatter(Number(value));
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
          id: 'success',
          min: 0,
          max: dataForRender?.maxSuccessCount,
          calculable: true,
          seriesIndex: 0,
          orient: 'horizontal',
          itemWidth: 14,
          itemHeight: (containerSize.width || 100) * 0.3,
          right: '45%',
          bottom: '5%',
          hoverLink: false,
          formatter: (value) => {
            if (value === setting.yMax) {
              return '';
            }
            return Math.floor(Number(value)).toLocaleString();
          },
          inRange: {
            color: ['#ffffff', dataForRender?.maxFailCount ? HeatmapColor.success : '#ffffff'],
          },
          range: successRange,
        },
        {
          id: 'fail',
          min: 0,
          max: dataForRender?.maxFailCount,
          calculable: true,
          seriesIndex: 1,
          orient: 'horizontal',
          itemWidth: 14,
          itemHeight: (containerSize.width || 100) * 0.3,
          left: '55%',
          bottom: '5%',
          hoverLink: false,
          formatter: (value) => {
            return Math.floor(Number(value)).toLocaleString();
          },
          inRange: {
            color: ['#ffffff', dataForRender?.maxFailCount ? HeatmapColor.failed : '#ffffff'],
          },
          range: failRange,
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
          data: dataForRender?.successData,
        },
        {
          name: 'failed',
          type: 'heatmap',
          data: dataForRender?.failData,
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
          style={{ height: '100%', width: '100%' }}
          onEvents={{
            mousedown: (params: any, echartsInstance: ECharts) => {
              console.log('mousedown', params);
              setStartCell(`${params.value[0]}-${params.value[1]}`);
              setEndCell(`${params.value[0]}-${params.value[1]}`);
            },
            mousemove: (params: any) => {
              if (!startCell) {
                return;
              }
              setEndCell(`${params.value[0]}-${params.value[1]}`);
            },
            mouseup: (params: any) => {
              console.log('mouseup', params, startCell, endCell);
              setStartCell('');
              setEndCell('');
            },
            datarangeselected: debounce((params: any) => {
              if (params.visualMapId === 'success') {
                setSuccessRange(params.selected);
              } else if (params.visualMapId === 'fail') {
                setFailRange(params.selected);
              }
            }, 300),
            // click: (params: any, echartsInstance: ECharts) => {
            //   console.log('click', params);
            //   setStartCell(`${params.value[0]}-${params.value[1]}`);
            //   // setRange([1000, 3000]);
            // },
          }}
        />
      </div>
    );
  },
);

export default HeatmapChartCore;
