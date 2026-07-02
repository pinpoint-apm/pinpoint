import React from 'react';
import * as echarts from 'echarts/core';
import { LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { useGetErrorAnalysisChartData } from '@pinpoint-fe/ui/src/hooks';
import { abbreviateNumber } from '@pinpoint-fe/ui/src/utils';
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

echarts.use([LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const formatErrorCount = (value: number) => abbreviateNumber(value, ['', 'K', 'M', 'G']);

export interface ErrorAnalysisChartFetcherProps {
  className?: string;
  emptyMessage?: string;
}

export const ErrorAnalysisChartFetcher = ({
  className,
  emptyMessage = 'No Data',
}: ErrorAnalysisChartFetcherProps) => {
  const { data } = useGetErrorAnalysisChartData();
  const { chartRef, chartInstanceRef, renderRef } = useEChartsInstance();

  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    const timestamps = data?.timestamp ?? [];
    const metricValues = data?.metricValueGroups[0]?.metricValues ?? [];
    const hasData =
      timestamps.length > 0 &&
      metricValues.some((mv) => mv.values && mv.values.some((v) => v >= 0));

    const series = metricValues.map(({ fieldName, values }) => ({
      name: fieldName,
      type: 'line' as const,
      data: values.map((v) => (v < 0 ? null : v)),
      showSymbol: false,
      smooth: false,
      lineStyle: {
        width: 1,
      },
      emphasis: {
        focus: 'series' as const,
      },
    }));

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
            right: 25,
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
            name: 'Error Count',
            nameGap: 40,
            nameLocation: 'middle',
            min: 0,
            axisLabel: {
              formatter: (value: number) => formatErrorCount(value),
            },
            axisLine: {
              show: true,
            },
            axisTick: {
              show: true,
            },
            splitLine: {
              show: false,
            },
            zlevel: 1,
          },
          tooltip: {
            show: true,
            trigger: 'axis',
            confine: true,
            formatter: (params: unknown) => formatAxisTooltip(params, formatErrorCount),
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
        // groupBy 변경 등으로 series 수가 줄어도 이전 series가 병합되어 잔존하지 않도록 항상 교체한다.
        { replaceMerge: ['series'] },
      );
    };

    renderRef.current = render;
    render();
  }, [data, emptyMessage, chartInstanceRef, chartRef, renderRef]);

  return <div className={cn('w-full h-full', className)} ref={chartRef} />;
};
