import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart as BarChartEcharts } from 'echarts/charts';
import { GridComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { abbreviateNumber, getMaxTickValue } from '@pinpoint-fe/ui/src/utils';
import { cn } from '../../../lib';

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
  const chartRef = React.useRef<HTMLDivElement>(null);
  const chartInstanceRef = React.useRef<echarts.EChartsType | null>(null);
  const chartData = React.useMemo(
    () => (typeof data === 'function' ? data(categories) : data || []),
    [data, categories],
  );

  // 차트 초기화
  React.useEffect(() => {
    if (!chartRef.current) return;

    const chart = echarts.init(chartRef.current);
    chartInstanceRef.current = chart;

    const wrapperElement = chartRef.current;
    if (!wrapperElement) return;
    const resizeObserver = new ResizeObserver(() => {
      chart.resize();
    });
    resizeObserver.observe(wrapperElement);

    return () => {
      resizeObserver.disconnect();
      chart.dispose();
    };
  }, []);

  // 데이터/옵션 변경 시 차트 업데이트
  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    const yAxisMax = chartData.length > 0 ? getMaxTickValue([chartData]) : undefined;

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
        ...(yAxisMax != null && { max: yAxisMax }),
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
      graphic:
        chartData.length === 0
          ? [
              {
                type: 'text',
                left: 'center',
                top: 'middle',
                style: {
                  text: emptyMessage,
                  fontSize: 14,
                  fill: '#999',
                  textAlign: 'center',
                },
              },
            ]
          : [],
    });
  }, [chartData, categories, colors, emptyMessage]);

  return (
    <div className="w-full h-full">
      {title}
      <div className={cn('w-full h-full', className)} ref={chartRef} />
    </div>
  );
};
