import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart as BarChartEcharts } from 'echarts/charts';
import { GridComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { abbreviateNumber, getMaxTickValue } from '@pinpoint-fe/ui/src/utils';
import { cn } from '../../../lib';
import { buildEmptyMessageGraphic } from '../../../lib/charts/echartsCommonOptions';
import { useEChartsInstance } from '../../../lib/charts/useEChartsInstance';

echarts.use([BarChartEcharts, GridComponent, CanvasRenderer]);

export interface ResponseAvgMaxChartProps {
  /**
   * The order of the data matches the order of the colors.
   */
  data: number[] | ((categories: ResponseAvgMaxChartProps['categories']) => number[]);
  categories?: string[];
  colors?: string[];
  title?: React.ReactNode;
  className?: string;
  emptyMessage?: string;
}

export const ResponseAvgMaxChart = ({
  data = [],
  categories = ['Avg', 'Max'],
  colors = ['#97E386', '#13B6E7'],
  title,
  className,
  emptyMessage = 'No Data',
}: ResponseAvgMaxChartProps) => {
  const { chartRef, chartInstanceRef } = useEChartsInstance();
  const chartData = React.useMemo(
    () => (typeof data === 'function' ? data(categories) : data || []),
    [data, categories],
  );

  // 데이터/옵션 변경 시 차트 업데이트
  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    // 데이터가 없거나 전부 0이면 값축 범위가 [0,0]으로 붕괴돼 축이 사라진다. 이때만 기본 최대값을
    // 줘서 축이 정상적으로 그려지게 한다. (값이 있으면 tick 기준 최대값으로 스케일)
    const valueMax = chartData.some((v) => v > 0) ? getMaxTickValue([chartData]) : 1;

    chartInstanceRef.current.setOption({
      grid: {
        top: 20,
        right: 20,
        bottom: 0,
        left: 0,
      },
      xAxis: {
        type: 'value',
        min: 0,
        max: valueMax,
        splitNumber: 3,
        zlevel: 1, // 그리드 라인이 데이터 막대 앞에 오도록
        axisLabel: {
          formatter: (value: number): string => abbreviateNumber(value, ['ms', 'sec']),
        },
        axisLine: { show: true },
        axisTick: { show: true },
        splitLine: {
          show: true,
          lineStyle: { type: 'dashed' },
        },
      },
      yAxis: {
        type: 'category',
        data: categories,
        zlevel: 1,
        axisLine: { show: true },
        axisTick: { show: true },
        inverse: true, // billboard.js rotated: true와 동일한 카테고리 순서(첫 항목이 위)
      },
      series: [
        {
          type: 'bar',
          zlevel: 0, // 데이터 막대는 축/그리드보다 뒤에
          data: chartData.map((value, index) => {
            const barColor = colors[index] ?? '#97E386';
            return {
              value,
              itemStyle: { color: barColor },
              label: { color: barColor },
            };
          }),
          barWidth: '60%',
          label: {
            show: chartData.length > 0,
            position: 'right',
            formatter: (params: { value: number }) => abbreviateNumber(params.value, ['ms', 'sec']),
            fontSize: 10,
          },
        },
      ],
      graphic: buildEmptyMessageGraphic(chartData.length > 0, emptyMessage, { fontSize: 14 }),
    });
  }, [chartData, categories, colors, emptyMessage, chartInstanceRef]);

  return (
    <div className="w-full h-full">
      {title}
      <div className={cn('w-full h-full', className)} ref={chartRef} />
    </div>
  );
};
