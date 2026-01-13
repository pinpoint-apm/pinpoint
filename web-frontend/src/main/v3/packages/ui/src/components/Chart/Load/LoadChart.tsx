import React from 'react';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { abbreviateNumber, addCommas } from '@pinpoint-fe/ui/src/utils';
import { formatInTimeZone } from 'date-fns-tz';
import { cn } from '../../../lib/utils';
import { colors } from '@pinpoint-fe/ui/src/constants';
import { useTimezone } from '@pinpoint-fe/ui/src/hooks';

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

export type LoadChartDataType = {
  dates?: number[];
  [key: string]: number[] | undefined;
};

export interface LoadChartProps {
  /**
   * The key value of datas and the key value of color match each other.
   */
  datas: LoadChartDataType | ((colors: LoadChartProps['chartColors']) => LoadChartDataType);
  chartColors?: {
    [key: string]: string;
  };
  title?: React.ReactNode;
  className?: string;
  emptyMessage?: string;
}

export const LoadChart = ({
  datas = {
    dates: [],
  },
  chartColors = {
    '1s': colors.fast,
    '100ms': colors.fast,
    '3s': colors.normal,
    '300ms': colors.normal,
    '5s': colors.delay,
    '500ms': colors.delay,
    Slow: colors.slow,
    Error: colors.error,
  },
  title,
  className,
  emptyMessage = 'No Data',
}: LoadChartProps) => {
  const [timezone] = useTimezone();
  const chartData = typeof datas === 'function' ? datas?.(chartColors) : datas;
  const chartRef = React.useRef<HTMLDivElement>(null);
  const chartInstanceRef = React.useRef<echarts.EChartsType | null>(null);
  // console.log('datas11', chartData);

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
    });
    resizeObserver.observe(wrapperElement);

    return () => {
      resizeObserver.disconnect();
      chart.dispose();
    };
  }, []);

  // 데이터 변경 시 차트 업데이트
  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    const dates = chartData.dates || [];
    const dataKeys = Object.keys(chartData).filter((key) => key !== 'dates');
    const hasData =
      dates.length > 0 &&
      dataKeys.length > 0 &&
      dataKeys.some((key) => chartData[key] && chartData[key].length > 0);

    // 시리즈 데이터 생성
    const series = dataKeys.map((key) => ({
      name: key,
      type: 'line' as const,
      // 데이터 값이 0임에도 stacked area chart 에 표현되는 버그가 있음
      // 그래서 0인 값은 아예 그려지지 않도록 null 로 변환
      // (https://github.com/apache/echarts/issues/16739)
      // data: chartData[key]?.map((value) => (value === 0 ? null : value)) || [],
      data: chartData[key]?.map((value, index) => {
        return [dates[index], value === 0 ? null : value];
      }),
      stack: 'total',
      areaStyle: {},
      smooth: false,
      step: 'middle',
      showSymbol: false,
      lineStyle: {
        width: 0,
      },
      itemStyle: {
        color: chartColors[key] || colors.fast,
      },
      emphasis: {
        focus: 'series',
      },
    }));

    chartInstanceRef.current.setOption({
      legend: {
        data: dataKeys,
        bottom: 0,
        icon: 'square',
        itemWidth: 10,
        itemHeight: 10,
        itemGap: 15,
      },
      grid: {
        top: 20,
        bottom: 60,
        right: 25,
        left: 0,
      },
      xAxis: {
        type: 'time',
        boundaryGap: false,
        splitNumber: 4,
        axisLabel: {
          showMinLabel: true,
          showMaxLabel: true,
          fontSize: 10,
          formatter: (value: number) => {
            try {
              return `${formatInTimeZone(value, timezone, 'MM.dd')}\n${formatInTimeZone(
                value,
                timezone,
                'HH:mm',
              )}`;
            } catch (error) {
              return '';
            }
          },
        },
        zlevel: 1,
      },
      yAxis: {
        type: 'value',
        min: 0,
        axisLine: {
          show: true,
        },
        zlevel: 1,
        splitNumber: 2,
        axisLabel: {
          formatter: (value: number): string => abbreviateNumber(value, ['', 'K', 'M', 'G']),
        },
        splitLine: {
          show: true,
          lineStyle: {
            type: 'dashed',
          },
        },
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'line',
        },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        formatter: (params: any) => {
          if (!Array.isArray(params) || params.length === 0) return '';
          const firstParam = params[0];
          const dateIndex = firstParam.dataIndex;
          const date = dates[dateIndex];
          const dateStr = date
            ? `${formatInTimeZone(new Date(date), timezone, 'MM.dd HH:mm')}`
            : '';
          const rows = params
            .map((param: { value: [number, number]; color: string; seriesName: string }) => {
              const value = addCommas(param.value?.[1]?.toString() || '0');
              const color = param.color;
              return `<div style="display: flex; align-items: center; gap: 8px;">
                <span style="display: inline-block; width: 10px; height: 10px; background-color: ${color};"></span>
                <span>${param.seriesName}: ${value}</span>
              </div>`;
            })
            .join('');
          return `<div style="margin-bottom: 4px;">${dateStr}</div>${rows}`;
        },
      },
      series,
      graphic: !hasData
        ? [
            {
              type: 'text',
              left: 'center',
              top: '30%',
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
  }, [chartData, chartColors, timezone, emptyMessage]);

  return (
    <div className="w-full h-full">
      {title}
      <div className={cn('w-full h-full', className)} ref={chartRef}></div>
    </div>
  );
};
