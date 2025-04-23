// echarts-for-react 로 작성했던 코드
// 현재는 사용하지 않지만 만약을 대비하여 남겨둠

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

type HeatmapChartProps = {
  isRealtime?: boolean;
  data?: GetHeatmapAppData.Response;
  setting: HeatmapSettingType;
  onDragEnd?: (
    dotData: { x1: number; y1: number; x2: number; y2: number },
    checkedLegends: string[],
  ) => void;
};

type DataForRender = {
  value: [string, string, number];
  itemStyle?: {
    color: string;
    opacity: number;
  };
};

function visualMapFormatter(value: string, range: [number, number] | undefined) {
  const valueString = Math.floor(Number(value))?.toLocaleString();

  if (!range) {
    return Number(value) === 0 ? '0~' : `~${valueString}`;
  }

  const isMin = Math.floor(Number(value)) < Math.floor(range?.[1]);
  const isMax = !range || Math.floor(Number(value)) > Math.floor(range?.[0]);

  if (isMin) {
    return `${valueString}~`;
  }
  if (isMax) {
    return `~${valueString}`;
  }
  return valueString;
}

const HeatmapChart = React.forwardRef(
  ({ isRealtime, data, setting, onDragEnd }: HeatmapChartProps, ref: React.Ref<HTMLDivElement>) => {
    const chartRef = React.useRef<ReactEChartsCore>(null);

    const [isMouseDown, setIsMouseDown] = React.useState(false);

    const [successRange, setSuccessRange] = React.useState<[number, number]>(); // 성공 범위: [시작, 끝]
    const [failRange, setFailRange] = React.useState<[number, number]>(); // 성공 범위: [시작, 끝]

    const [startCell, setStartCell] = React.useState<any>(); // 시작 셀: x-y
    const [endCell, setEndCell] = React.useState<any>(); // 끝 셀: x-y

    React.useEffect(() => {
      const wrapperElement = chartRef.current?.getEchartsInstance()?.getDom();

      if (!wrapperElement) return;

      const resizeObserver = new ResizeObserver(() => {
        chartRef.current?.getEchartsInstance()?.resize();
      });
      resizeObserver.observe(wrapperElement);

      return () => {
        resizeObserver.disconnect();
      };
    }, []);

    // realtime일 경우 사용하지 않음
    const maxCount = React.useMemo(() => {
      let success = 0;
      let fail = 0;

      const { heatmapData } = data || {};

      if (heatmapData) {
        for (const data of heatmapData) {
          for (const cell of data.cellDataList) {
            success = Math.max(success, cell.successCount);
            fail = Math.max(fail, cell.failCount);
          }
        }
      }

      return { success, fail };
    }, [data]);

    const dataForRender = React.useMemo(() => {
      const successData: DataForRender['value'][] = [];
      const failData: DataForRender['value'][] = [];
      const coverData: DataForRender[] = []; // 가장 위에 덮어져서 tooltip, select 이벤트를 받기 위한 것

      const { heatmapData } = data || {};
      heatmapData?.forEach((row) => {
        row?.cellDataList?.forEach((cell) => {
          coverData.push({
            value: [String(row.timestamp), String(cell.elapsedTime), 0],
            itemStyle: {
              color: 'transparent',
              opacity: isMouseDown ? 0 : 1,
            },
          });

          if (cell?.successCount) {
            successData.push([String(row.timestamp), String(cell.elapsedTime), cell.successCount]);
          }

          if (cell?.failCount) {
            failData.push([String(row.timestamp), String(cell.elapsedTime), cell.failCount]);
          }
        });
      });

      return { successData, failData, coverData };
    }, [data, maxCount, isMouseDown]);

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

    const getDragRect = React.useCallback(() => {
      if (!startCell || !endCell) {
        return [];
      }

      const startX = startCell.event?.target?.shape?.x;
      const startY = startCell.event?.target?.shape?.y;
      const endX = endCell.event?.target?.shape?.x;
      const endY = endCell.event?.target?.shape?.y;
      const cellWidth = startCell.event?.target?.shape?.width;
      const cellHeight = startCell.event?.target?.shape?.height;

      if (startX <= endX && startY <= endY) {
        // 오른쪽 아래로
        return {
          x: startX,
          y: startY,
          width: endX - startX + cellWidth,
          height: endY - startY + cellHeight,
        };
      } else if (startX <= endX && startY >= endY) {
        // 오른쪽 위로
        return {
          x: startX,
          y: endY,
          width: endX - startX + cellWidth,
          height: startY - endY + cellHeight,
        };
      } else if (startX >= endX && startY <= endY) {
        // 왼쪽 아래로
        return {
          x: endX,
          y: startY,
          width: startX - endX + cellWidth,
          height: endY - startY + cellHeight,
        };
      } else if (startX >= endX && startY >= endY) {
        // 왼쪽 위로
        return {
          x: endX,
          y: endY,
          width: startX - endX + cellWidth,
          height: startY - endY + cellHeight,
        };
      }
    }, [startCell, endCell]);

    const handleDragEnd = React.useCallback(() => {
      if (!startCell || !endCell) {
        return;
      }

      const [startX, startY] = startCell?.data?.value;
      const [endX, endY] = endCell?.data?.value;

      const x1 = Math.min(startX, endX);
      const x2 = Math.max(startX, endX);

      const y1 = Math.max(startY, endY);

      const bottom = Math.min(startY, endY);
      const bottomIndex = yAxisData?.findIndex((yValue) => Number(yValue) === Number(bottom));
      const y2 = bottomIndex <= 0 ? 0 : Number(yAxisData?.[bottomIndex - 1]);

      const checkedLegends = [];

      if (!successRange || Math.floor(successRange?.[0]) !== Math.floor(successRange?.[1])) {
        checkedLegends.push('success');
      }

      if (!failRange || Math.floor(failRange?.[0]) !== Math.floor(failRange?.[1])) {
        checkedLegends.push('fail');
      }

      onDragEnd?.(
        {
          x1,
          x2,
          y1: y1 === Number(yAxisData?.[yAxisData?.length - 1]) ? Number.MAX_SAFE_INTEGER : y1,
          y2,
        },
        checkedLegends,
      );
    }, [startCell, endCell, yAxisData]);

    const option: EChartsOption = {
      tooltip: {
        show: !!isMouseDown ? false : true,
        borderColor: colors.gray[300],
        textStyle: {
          fontFamily: 'inherit',
          fontSize: 8,
        },
        formatter: (params: any) => {
          const { data } = params;
          const [timestamp, elapsedTime] = data?.value;

          const failedCount = dataForRender?.failData.find((item: [string, string, number]) => {
            return item[0] === timestamp && item[1] === elapsedTime;
          })?.[2];

          const successCount = dataForRender?.successData.find((item: [string, string, number]) => {
            return item[0] === timestamp && item[1] === elapsedTime;
          })?.[2];

          return `
        <div style="display: flex; flex-direction: column; gap: 5px; padding: 2px;">
          <div style="margin-bottom: 5px;"><strong>${defaultTickFormatter(Number(timestamp))}</strong></div>
          ${['success', 'fail']
            .map((type) => {
              const count = type === 'success' ? successCount : failedCount;
              const color = HeatmapColor[type as 'success' | 'fail'][5];

              return `
              <div style="display: flex; justify-content: space-between; gap: 5px;">
                <div style="display: flex; gap: 6px; align-items: center;">
                  <div style="width: 8px; height: 8px; background: ${color}; border: ${count === 0 ? '1px solid black' : 'none'};"></div>${capitalize(type)}
                </div>
                <div>${count === undefined ? 0 : Number(count).toLocaleString()}</div>
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
        top: '20px',
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
        offset: 1,
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
          max: isRealtime ? 5000 : maxCount.success,
          calculable: true,
          seriesIndex: 0,
          orient: 'horizontal',
          itemWidth: 12,
          itemHeight: (chartRef?.current?.getEchartsInstance()?.getWidth() || 600) * 0.3,
          right: '45%',
          bottom: '0%',
          hoverLink: false,
          formatter: (value) => {
            return visualMapFormatter(value as string, successRange);
          },
          inRange: {
            color: HeatmapColor.success,
          },
          range: successRange,
        },
        {
          id: 'fail',
          min: 0,
          max: isRealtime ? 100 : maxCount.fail,
          calculable: true,
          seriesIndex: 1,
          orient: 'horizontal',
          itemWidth: 12,
          itemHeight: (chartRef?.current?.getEchartsInstance()?.getWidth() || 600) * 0.3,
          left: '55%',
          bottom: '0%',
          hoverLink: false,
          formatter: (value) => {
            return visualMapFormatter(value as string, failRange);
          },
          inRange: {
            color: HeatmapColor.fail,
          },
          range: failRange,
        },
        {
          id: 'cover',
          show: false,
          seriesIndex: 2,
        },
      ],
      graphic: [
        {
          type: 'text',
          bottom: 38,
          left: 'center',
          style: {
            text: `Success {boldSuccess|${Math.floor(totalSuccessCount).toLocaleString()}}    Failed {boldFailed|${Math.floor(totalFailedCount).toLocaleString()}}`,
            fontSize: 15,
            fill: colors.gray[500],
            rich: {
              boldSuccess: {
                fontWeight: 'bold',
                fill: HeatmapColor.success[5],
              },
              boldFailed: {
                fontWeight: 'bold',
                fill: HeatmapColor.fail[5],
              },
            },
          },
        },
        {
          type: 'rect',
          z: 10,
          shape: getDragRect(),
          style: {
            fill: 'rgba(225,225,225,0.4)',
            stroke: '#469ae4',
            lineWidth: 1,
          },
          silent: true,
        },
      ],
      series: [
        {
          name: 'success',
          type: 'heatmap',
          data: dataForRender?.successData,
        },
        {
          name: 'fail',
          type: 'heatmap',
          data: dataForRender?.failData,
        },
        {
          name: 'cover',
          type: 'heatmap',
          data: dataForRender?.coverData,
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
      <div ref={ref} className="relative w-full h-full">
        <ReactEChartsCore
          ref={chartRef}
          echarts={echarts}
          option={option}
          style={{ height: '100%', width: '100%' }}
          onChartReady={(chartInstance) => {
            // mouseup, mousedown 이벤트를 동시 on 적용시 visualMap이 동작하지 않아 mouseup만 적용
            chartInstance.getZr().on('mouseup', () => {
              setIsMouseDown(false);
              handleDragEnd();
              setStartCell(undefined);
              setEndCell(undefined);
            });
          }}
          onEvents={{
            mousedown: (params: any) => {
              setIsMouseDown(true);
              if (!startCell) {
                setIsMouseDown(true);
                setStartCell(params);
                setEndCell(params);
              }
            },
            mousemove: (params: any) => {
              if (!isMouseDown) {
                return;
              }
              if (!startCell) {
                setStartCell(params);
              }
              setEndCell(params);
            },
            datarangeselected: debounce((params: any) => {
              if (params.visualMapId === 'success') {
                setSuccessRange(params.selected);
              } else if (params.visualMapId === 'fail') {
                setFailRange(params.selected);
              }
            }, 300),
          }}
        />
      </div>
    );
  },
);

export default HeatmapChart;
