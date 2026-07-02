import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart as BarChartEcharts, LineChart as LineChartEcharts } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { UrlStatChartType as UrlStatChartApi } from '@pinpoint-fe/ui/src/constants';
import { cn } from '../../../lib';
import {
  getGridBottom,
  LEGEND_ICON_WIDTH,
  LEGEND_ITEM_GAP,
} from '../../../lib/charts/echartsLegendLayout';
import { useEChartsInstance } from '../../../lib/charts/useEChartsInstance';
import {
  formatAxisTooltip,
  formatCategoryDateLabel,
} from '../../../lib/charts/echartsTimeSeriesFormat';

echarts.use([
  BarChartEcharts,
  LineChartEcharts,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer,
]);

export interface UrlStatEChartsChartProps {
  chartType: 'bar' | 'line';
  yAxisName: 'Total Count' | 'Failure Count' | 'Apdex Score' | 'Latency (ms)';
  chartColors: string[];
  formatter?: (value: number) => string;
  data: UrlStatChartApi.Response | null;
  className?: string;
  emptyMessage?: string;
}

export const UrlStatEChartsChart = ({
  chartType,
  yAxisName,
  chartColors,
  formatter,
  data,
  className,
  emptyMessage = 'No Data',
}: UrlStatEChartsChartProps) => {
  const { chartRef, chartInstanceRef, renderRef } = useEChartsInstance();

  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    const hasData =
      data &&
      data.timestamp.length > 0 &&
      data.metricValueGroups[0]?.metricValues?.some(
        (mv) => mv.values && mv.values.some((v) => v >= 0),
      );

    const timestamps = data?.timestamp ?? [];
    const metricValues = data?.metricValueGroups[0]?.metricValues ?? [];

    const series = metricValues.map(({ fieldName, values }, seriesIndex) => {
      if (chartType === 'line') {
        return {
          name: fieldName,
          type: 'line' as const,
          data: values.map((v) => (v < 0 ? null : v)),
          showSymbol: false,
          smooth: false,
          lineStyle: {
            width: 1,
          },
          itemStyle: {
            color: chartColors[seriesIndex] ?? chartColors[0],
          },
          emphasis: {
            focus: 'series',
          },
        };
      }

      return {
        name: fieldName,
        type: 'bar' as const,
        stack: 'total',
        data: values.map((v) => (v < 0 ? null : v)),
        itemStyle: {
          color: chartColors[seriesIndex],
        },
      };
    });

    const legendNames = metricValues.map((mv) => mv.fieldName);

    const render = () => {
      const chart = chartInstanceRef.current;
      if (!chart) return;

      const containerWidth = chartRef.current?.clientWidth ?? 0;
      // legend가 줄바꿈되는 행 수를 반영해 하단 여백을 확보 → legend(하단)와 x축 라벨이 겹치지 않음.
      const gridBottom = getGridBottom(legendNames, containerWidth);

      chart.setOption(
        {
          animation: false,
          legend: {
            data: legendNames,
            bottom: 0,
            icon: 'square',
            itemWidth: LEGEND_ICON_WIDTH,
            itemHeight: 10,
            itemGap: LEGEND_ITEM_GAP,
          },
          grid: {
            top: 20,
            bottom: gridBottom,
            right: 30,
            left: 0,
          },
          xAxis: {
            type: 'category',
            data: timestamps,
            axisLabel: {
              show: true,
              formatter: formatCategoryDateLabel,
              showMaxLabel: true,
              showMinLabel: true,
            },
            axisTick: { show: false },
            zlevel: 1,
          },
          yAxis: {
            type: 'value',
            name: yAxisName,
            nameGap: 40,
            nameLocation: 'middle',
            min: 0,
            axisLabel: {
              formatter: formatter ?? ((value: number) => String(value)),
            },
            axisLine: {
              show: true,
            },
            axisTick: {
              show: true,
            },
            splitLine: {
              show: true,
              lineStyle: { type: 'dashed' },
            },
            zlevel: 1,
          },
          tooltip: {
            show: true,
            trigger: 'axis',
            confine: true,
            formatter: (params: unknown) =>
              formatAxisTooltip(params, formatter ?? ((value: number) => String(value))),
          },
          series,
          graphic: !hasData
            ? [
                {
                  type: 'text',
                  left: 'center',
                  top: 'middle',
                  style: {
                    text: emptyMessage,
                    fontSize: 18,
                    fill: '#999',
                    textAlign: 'center',
                  },
                },
              ]
            : [],
        },
        // chartType/data 변경(예: bar→line 탭 전환)으로 series 수가 줄어도
        // 이전 series가 병합되어 잔존하지 않도록 series는 항상 교체한다.
        { replaceMerge: ['series'] },
      );
    };

    renderRef.current = render;
    render();
  }, [
    data,
    emptyMessage,
    chartType,
    yAxisName,
    chartColors,
    formatter,
    chartInstanceRef,
    chartRef,
    renderRef,
  ]);

  return <div className={cn('w-full h-full', className)} ref={chartRef} />;
};
