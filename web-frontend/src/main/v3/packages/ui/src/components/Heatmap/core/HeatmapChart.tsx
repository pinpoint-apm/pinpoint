/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';
import { colors, GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';
import { defaultTickFormatter } from '@pinpoint-fe/ui/src/components/ReChart';
import { capitalize } from 'lodash';
import { HeatmapSettingType } from './HeatmapSetting';
import { formatInTimeZone } from 'date-fns-tz';
import { useTimezone } from '@pinpoint-fe/ui/src/hooks';

import * as echarts from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { HeatmapChart as HeatmapChartEcharts } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  VisualMapComponent,
  GraphicComponent,
} from 'echarts/components';
// 등록
echarts.use([
  CanvasRenderer, // 캔버스 렌더링
  HeatmapChartEcharts, // 히트맵 차트
  GridComponent, // 기본 그리드
  TooltipComponent, // 마우스 오버 툴팁
  VisualMapComponent, // 색상 범례
  GraphicComponent, // 커스텀 그래픽 (선, 박스 등)
]);

type DataValue = [string, string, number];

type HeatmapChartProps = {
  isRealtime?: boolean;
  data?: GetHeatmapAppData.Response;
  setting: HeatmapSettingType;
  onDragEnd?: (
    dotData: { x1: number; y1: number; x2: number; y2: number },
    checkedLegends: string[],
  ) => void;
};

function visualMapFormatter(value: string, range: [number, number] | undefined) {
  const valueString = Math.floor(Number(value))?.toLocaleString();

  if (!range) {
    return Number(value) === 0 ? '0 ~' : `~ ${valueString}`;
  }

  if (Math.floor(Number(value)) >= Math.floor(Number(range[1]))) {
    return '~ ' + valueString;
  } else if (Math.floor(Number(value)) <= Math.floor(Number(range[0]))) {
    return valueString + ' ~';
  } else {
    return valueString; // 중간 조정 시 일반 출력
  }
}

const HeatmapChart = ({
  isRealtime,
  data,
  setting,
  onDragEnd,
  ref,
}: HeatmapChartProps & { ref?: React.Ref<HTMLDivElement> }) => {
  const chartRef = React.useRef(null);
  const chartInstanceRef = React.useRef<echarts.EChartsType | null>(null);
  const [timezone] = useTimezone();
  const [isMouseDown, setIsMouseDown] = React.useState(false);

  const [startCell, setStartCell] = React.useState<any>(); // 시작 셀: x-y
  const [endCell, setEndCell] = React.useState<any>(); // 끝 셀: x-y
  // 차트 초기화
  React.useEffect(() => {
    if (!chartRef.current) return;

    const chart = echarts.init(chartRef.current);
    chartInstanceRef.current = chart;

    // chart resize
    const wrapperElement = chartRef.current;
    if (!wrapperElement) return;
    const resizeObserver = new ResizeObserver(() => {
      chart.resize();
      chart.setOption({
        visualMap: [
          {
            itemHeight: chart.getWidth() * 0.3,
          },
          {
            itemHeight: chart.getWidth() * 0.3,
          },
        ],
      });
    });
    resizeObserver.observe(wrapperElement);

    const handleMouseDown = (parmas: any) => {
      setIsMouseDown(true);
      setStartCell(parmas);
      setEndCell(parmas);
    };

    chart.on('mousedown', handleMouseDown);

    return () => {
      chart.off('mousedown', handleMouseDown);
      resizeObserver.disconnect();
      chart.dispose();
    };
  }, []);

  React.useEffect(() => {
    const handleMouseMove = (params: any) => {
      if (!isMouseDown) {
        return;
      }
      if (!startCell) {
        setStartCell(params);
      }
      setEndCell(params);
    };

    chartInstanceRef.current?.on('mousemove', handleMouseMove);

    return () => {
      if (chartInstanceRef.current && !chartInstanceRef.current.isDisposed()) {
        chartInstanceRef.current.off('mousemove', handleMouseMove);
      }
    };
  }, [isMouseDown, startCell]);

  // data 변경 시 업데이트
  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    let successMaxCount = 0;
    let failMaxCount = 0;
    const successData: DataValue[] = [];
    const failData: DataValue[] = [];
    const coverData: DataValue[] = []; // 가장 위에 덮어져서 tooltip, select 이벤트를 받기 위한 것

    const { heatmapData } = data || {};
    heatmapData?.forEach((row) => {
      row?.cellDataList?.forEach((cell) => {
        successMaxCount = Math.max(successMaxCount, cell.successCount);
        failMaxCount = Math.max(failMaxCount, cell.failCount);

        coverData.push([String(row.timestamp), String(cell.elapsedTime), 0]);

        if (cell?.successCount) {
          successData.push([String(row.timestamp), String(cell.elapsedTime), cell.successCount]);
        }

        if (cell?.failCount) {
          failData.push([String(row.timestamp), String(cell.elapsedTime), cell.failCount]);
        }
      });
    });

    const xAxisData = heatmapData?.map((row) => String(row.timestamp)) || [];
    const yAxisData =
      heatmapData?.[heatmapData?.length - 1]?.cellDataList
        ?.map((cell) => String(cell.elapsedTime))
        ?.filter((yValue) => Number(yValue) >= setting.yMin && Number(yValue) <= setting.yMax) ||
      [];
    const totalSuccessCount = data?.summary?.totalSuccessCount || 0;
    const totalFailCount = data?.summary?.totalFailCount || 0;

    chartInstanceRef.current.setOption({
      grid: {
        show: true,
        left: setting.yMax.toString().length * 10,
        right: '22px',
        top: '20px',
        bottom: '100px',
      },
      xAxis: {
        type: 'category',
        data: xAxisData?.sort((a, b) => Number(a) - Number(b)),
        axisLabel: {
          interval: '1',
          showMaxLabel: true,
          showMinLabel: true,
          formatter: (value: string) => {
            return `${formatInTimeZone(Number(value), timezone, 'yyyy.MM.dd')}\n${formatInTimeZone(Number(value), timezone, 'HH:mm:ss')}`;
          },
        },
      },
      yAxis: {
        type: 'category',
        data: yAxisData,
        offset: 0.5,
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
          max: isRealtime ? setting?.visualMapSuccessMax : successMaxCount,
          calculable: true,
          seriesIndex: 0,
          orient: 'horizontal',
          itemWidth: 12,
          right: '48%',
          bottom: '-2%',
          hoverLink: false,
          formatter: (value: any) => {
            const visualMaps = chartInstanceRef.current?.getOption()?.visualMap || [];
            const successRange = (visualMaps as any)?.find((vm: any) => vm.id === 'success')?.range;

            return visualMapFormatter(value as string, successRange);
          },
          inRange: {
            color: [colors.green[100], colors.green[900]],
          },
          handleStyle: {
            borderColor: colors.gray[300],
            borderWidth: 2,
          },
        },
        {
          id: 'fail',
          min: 0,
          max: isRealtime ? setting?.visualMapFailMax : failMaxCount,
          calculable: true,
          seriesIndex: 1,
          orient: 'horizontal',
          itemWidth: 12,
          left: '56%',
          bottom: '-2%',
          hoverLink: false,
          formatter: (value: any) => {
            const visualMaps = chartInstanceRef.current?.getOption()?.visualMap || [];
            const failRange = (visualMaps as any)?.find((vm: any) => vm.id === 'fail')?.range;

            return visualMapFormatter(value as string, failRange);
          },
          inRange: {
            color: [colors.red[100], colors.red[900]],
          },
          handleStyle: {
            borderColor: colors.gray[300],
            borderWidth: 2,
          },
        },
        {
          id: 'cover',
          show: false,
          seriesIndex: 2,
          left: '-100',
          inRange: {
            color: 'transparent',
          },
        },
      ],
      tooltip: {
        show: !!isMouseDown ? false : true,
        borderColor: colors.gray[300],
        textStyle: {
          fontFamily: 'inherit',
          fontSize: 8,
        },
        formatter: (params: any) => {
          const { data } = params;
          const [timestamp, elapsedTime] = data;

          const failCount = failData.find((item: [string, string, number]) => {
            return item[0] === timestamp && item[1] === elapsedTime;
          })?.[2];

          const successCount = successData.find((item: [string, string, number]) => {
            return item[0] === timestamp && item[1] === elapsedTime;
          })?.[2];

          return `
							<div style="display: flex; flex-direction: column; gap: 5px; padding: 2px;">
								<div style="margin-bottom: 5px;"><strong>${defaultTickFormatter(Number(timestamp))}</strong></div>
								${['success', 'failed']
                  .map((type) => {
                    const count = type === 'success' ? successCount : failCount;
                    const color = colors[type === 'success' ? 'green' : 'red'][500];

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
      graphic: [
        {
          type: 'group',
          right: '55%',
          bottom: 40,
          children: [
            {
              type: 'circle',
              left: 0,
              top: 0,
              shape: { r: 6 },
              style: { fill: '#34D399' },
            },
            {
              type: 'text',
              left: 14,
              top: 0,
              style: {
                text: 'Success',
                fontSize: 14,
                fill: '#6B7280',
                fontFamily: 'sans-serif',
              },
            },
            {
              type: 'text',
              top: -1,
              left: 65,
              style: {
                text: `${Math.floor(totalSuccessCount).toLocaleString()}`,
                fontSize: 18,
                fontWeight: 'bold',
                fill: '#111827',
                fontFamily: 'sans-serif',
              },
            },
          ],
        },
        {
          type: 'group',
          left: '65%',
          bottom: 40,
          children: [
            {
              type: 'circle',
              left: 0,
              top: 0,
              shape: { r: 6 },
              style: { fill: '#EF4444' },
            },
            {
              type: 'text',
              left: 14,
              top: 0,
              style: {
                text: 'Failed',
                fontSize: 14,
                fill: '#6B7280',
                fontFamily: 'sans-serif',
              },
            },
            {
              type: 'text',
              left: 55,
              top: -1,
              style: {
                text: `${Math.floor(totalFailCount).toLocaleString()}`,
                fontSize: 18,
                fontWeight: 'bold',
                fill: '#111827',
                fontFamily: 'sans-serif',
              },
            },
          ],
        },
      ],
      series: [
        {
          name: 'success',
          type: 'heatmap',
          data: successData,
        },
        {
          name: 'fail',
          type: 'heatmap',
          data: failData,
        },
        {
          name: 'cover',
          type: 'heatmap',
          data: coverData,
          emphasis: {
            itemStyle: {
              borderColor: '#333',
              borderWidth: 1,
            },
          },
          cursor: 'crosshair',
        },
      ],
    });
  }, [data, isMouseDown, setting]);

  // dragRect
  React.useEffect(() => {
    if (!chartInstanceRef.current || !data) return;

    function getDragRect() {
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
    }

    chartInstanceRef.current.setOption({
      graphic: [
        {
          type: 'rect',
          id: 'drag-rect',
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
    });
  }, [startCell, endCell]);

  const timeGap = React.useMemo(() => {
    try {
      if (!data) {
        return 0;
      }

      const { heatmapData } = data;
      const firstTimestamp = heatmapData?.[0]?.timestamp;
      const secondTimestamp = heatmapData?.[1]?.timestamp;

      return Math.abs(Number(firstTimestamp) - Number(secondTimestamp)) || 0;
    } catch (err) {
      return 0;
    }
  }, [data]);

  const handleDragEnd = React.useCallback(() => {
    if (
      !startCell ||
      !endCell ||
      startCell?.componentType !== 'series' ||
      endCell?.componentType !== 'series'
    ) {
      return;
    }

    const startData = startCell?.data;
    const endData = endCell?.data;

    if (!startData || !endData) {
      return;
    }

    const [startX, startY] = startData;
    const [endX, endY] = endData;
    const yAxisData = (chartInstanceRef.current?.getOption()?.yAxis as any)[0].data || [];
    const visualMaps = chartInstanceRef.current?.getOption()?.visualMap || [];
    const successRange = (visualMaps as any)?.find((vm: any) => vm.id === 'success')?.range;
    const failRange = (visualMaps as any)?.find((vm: any) => vm.id === 'fail')?.range;

    const x1 = Math.min(startX, endX);
    const x2 = Math.max(startX, endX) + timeGap - 1;

    const y1 = Math.max(startY, endY);

    const bottom = Math.min(startY, endY);
    const bottomIndex = yAxisData?.findIndex((yValue: string) => Number(yValue) === Number(bottom));
    const y2 = bottomIndex <= 0 ? 0 : Number(yAxisData?.[bottomIndex - 1]);

    const checkedLegends: any = [];

    if (!successRange || Math.floor(successRange?.[1]) !== 0) {
      checkedLegends.push('success');
    }

    if (!failRange || Math.floor(failRange?.[1]) !== 0) {
      checkedLegends.push('failed');
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
  }, [startCell, endCell, timeGap]);

  return (
    <div
      ref={ref}
      className="relative w-full h-full"
      onMouseDown={() => {
        setIsMouseDown(true);
      }}
      onMouseUp={() => {
        setIsMouseDown(false);
        setStartCell(undefined);
        setEndCell(undefined);

        handleDragEnd();

        try {
          chartInstanceRef.current?.setOption({
            graphic: [
              {
                id: 'drag-rect',
                $action: 'remove',
              },
            ],
          });
        } catch (err) {
          console.error('Error onMouseUp', err);
        }
      }}
    >
      <div ref={chartRef} style={{ width: '100%', height: '100%' }} />
    </div>
  );
};

export default HeatmapChart;
