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
import { buildBottomLegend, getGridBottom } from '../../../lib/charts/echartsLegendLayout';
import { buildEmptyMessageGraphic } from '../../../lib/charts/echartsCommonOptions';
import { formatAxisTooltip } from '../../../lib/charts/echartsTimeSeriesFormat';
import { useEChartsInstance } from '../../../lib/charts/useEChartsInstance';

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
  const { chartRef, chartInstanceRef, renderRef } = useEChartsInstance();

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
      // 데이터 값이 0임에도 stacked area chart 에 표현되는 버그가 있어 0인 값은 아예 그려지지 않도록
      // null 로 변환한다. (https://github.com/apache/echarts/issues/16739)
      data: chartData[key]?.map((value, index) => [dates[index], value === 0 ? null : value]),
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
        focus: 'series' as const,
      },
    }));

    const render = () => {
      const chart = chartInstanceRef.current;
      if (!chart) return;

      const containerWidth = chartRef.current?.clientWidth ?? 0;
      const gridBottom = getGridBottom(dataKeys, containerWidth);

      chart.setOption({
        legend: buildBottomLegend(dataKeys),
        grid: {
          top: 20,
          bottom: gridBottom,
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
          // 0 은 stacked area 버그 때문에 null 로 치환돼 있으므로 tooltip 에선 0 으로 표시하고,
          // 날짜 헤더는 사용자 지정 타임존을 반영한다.
          formatter: (params: unknown) =>
            formatAxisTooltip(params, (value) => addCommas(String(value)), {
              nullBehavior: 'zero',
              formatDate: (axisValue) => {
                try {
                  return formatInTimeZone(axisValue, timezone, 'MM.dd HH:mm');
                } catch (error) {
                  return '';
                }
              },
            }),
        },
        series,
        graphic: buildEmptyMessageGraphic(hasData, emptyMessage, { fontSize: 14, top: '30%' }),
      });
    };

    renderRef.current = render;
    render();
  }, [chartData, chartColors, timezone, emptyMessage, chartInstanceRef, chartRef, renderRef]);

  return (
    <div className="w-full h-full">
      {title}
      <div className={cn('w-full h-full', className)} ref={chartRef}></div>
    </div>
  );
};
