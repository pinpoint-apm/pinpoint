import React from 'react';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { cn } from '../../../lib';
import { buildEmptyMessageGraphic } from '../../../lib/charts/echartsCommonOptions';
import { useEChartsInstance } from '../../../lib/charts/useEChartsInstance';

echarts.use([LineChart, GridComponent, CanvasRenderer]);

export interface UrlStatDefaultChartProps {
  className?: string;
  emptyMessage?: string;
}

export const UrlStatDefaultChart = ({
  className,
  emptyMessage = 'No Data',
}: UrlStatDefaultChartProps) => {
  const { chartRef, chartInstanceRef } = useEChartsInstance();

  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    chartInstanceRef.current.setOption({
      animation: false,
      grid: {
        top: 20,
        bottom: 20,
        right: 25,
        left: 25,
      },
      xAxis: {
        type: 'category',
        show: true,
        boundaryGap: true,
        data: [''],
        axisLine: { show: true },
        axisTick: { show: true },
        axisLabel: { show: true },
      },
      yAxis: {
        type: 'value',
        min: 0,
        max: 1,
        splitNumber: 1,
        axisLine: { show: true },
        axisTick: { show: true },
        splitLine: { show: false },
        axisLabel: { show: true },
      },
      series: [
        {
          type: 'line',
          data: [null],
          showSymbol: false,
          lineStyle: { width: 0 },
        },
      ],
      graphic: buildEmptyMessageGraphic(false, emptyMessage),
    });
  }, [emptyMessage, chartInstanceRef]);

  return <div className={cn('w-full h-full', className)} ref={chartRef} />;
};
