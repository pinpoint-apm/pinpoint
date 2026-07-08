import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart, LineChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { cn, DEFAULT_CHART_CONFIG, InspectorChartOptions } from '../../../lib';
import { InspectorAgentChart, InspectorApplicationChart } from '@pinpoint-fe/ui/src/constants';
import { getFormat } from '@pinpoint-fe/ui/src/utils';
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
  LineChart,
  BarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  GraphicComponent,
  CanvasRenderer,
]);

export interface ChartCoreProps {
  data: InspectorAgentChart.Response | InspectorApplicationChart.Response;
  chartOptions?: InspectorChartOptions;
  className?: string;
  emptyMessage?: string;
  style?: React.CSSProperties;
}

const EMPTY_CHART_OPTIONS: InspectorChartOptions = {
  seriesOptions: {},
  yAxis: [],
  legendShow: true,
};

export const ChartCore = ({
  data,
  chartOptions = EMPTY_CHART_OPTIONS,
  className,
  emptyMessage = 'No Data',
  style,
}: ChartCoreProps) => {
  const { seriesOptions, yAxis, group, legendShow, tooltipFormatter } = chartOptions;
  const { chartRef, chartInstanceRef, renderRef } = useEChartsInstance({ group });

  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    const timestamps = data?.timestamp ?? [];
    // tooltip 전용 필드(chartType: 'tooltip')는 시리즈로 그리지 않고 tooltipFormatter 가 별도로 처리한다.
    const metricValues = (data?.metricValues ?? []).filter((mv) => mv.chartType !== 'tooltip');
    const hasData =
      timestamps.length > 0 &&
      metricValues.some((mv) => mv.valueList && mv.valueList.some((v) => v >= 0));

    const yAxisList = yAxis.length > 0 ? yAxis : [{ unit: metricValues[0]?.unit ?? '' }];

    // unit 별 최대값.
    const maxByUnit = new Map<string, number>();
    metricValues.forEach((mv) => {
      const unit = seriesOptions[mv.fieldName]?.unit ?? mv.unit;
      const m = Math.max(0, ...mv.valueList.filter((v) => v >= 0));
      maxByUnit.set(unit, Math.max(maxByUnit.get(unit) ?? 0, m));
    });

    const series = metricValues.map((mv) => {
      const seriesOption = seriesOptions[mv.fieldName];
      const unit = seriesOption?.unit ?? mv.unit;
      const yAxisIndex = Math.max(
        0,
        yAxisList.findIndex((axis) => axis.unit === unit),
      );
      // 음수(-1)는 BE 의 미수집(uncollected) 센티넬이므로 null 로 두어 선을 끊는다.
      const seriesData = mv.valueList.map((v) => (v < 0 ? null : v));
      const dashed = seriesOption?.dashed;

      return {
        name: seriesOption?.name ?? mv.fieldName,
        type: seriesOption?.type === 'bar' ? ('bar' as const) : ('line' as const),
        yAxisIndex,
        data: seriesData,
        stack: seriesOption?.stack,
        smooth: !!seriesOption?.smooth,
        showSymbol: false,
        ...(seriesOption?.area ? { areaStyle: {} } : {}),
        lineStyle: {
          width: 1,
          ...(dashed ? { type: 'dashed' as const } : {}),
        },
        ...(seriesOption?.color ? { itemStyle: { color: seriesOption.color } } : {}),
        // 그래프 또는 legend 항목에 hover 하면 다른 시리즈를 흐리게 해 hover 한 값을 강조한다.
        // (SystemMetric 차트와 동일한 동작)
        emphasis: {
          focus: 'series' as const,
        },
      };
    });

    const legendNames = legendShow ? series.map((s) => s.name) : [];

    const yAxisEChartsOption = yAxisList.map((axis, index) => {
      const axisMax = maxByUnit.get(axis.unit) ?? 0;
      const formatValue = getFormat(axis.unit);
      // 모든 축을 0 바닥 기준으로 그린다(보조 축 포함). 값이 없으면(전부 0/미수집) 축이 납작해지지 않도록
      // 기본 최대값을 준다. 실측 음수 값은 없고 -1 은 미수집 센티넬(null 처리)이므로 min 은 항상 0.
      const range = { min: 0, max: axisMax > 0 ? undefined : DEFAULT_CHART_CONFIG.DEFAULT_MAX };
      return {
        type: 'value' as const,
        position: index === 0 ? ('left' as const) : ('right' as const),
        ...(index > 1 ? { offset: (index - 1) * DEFAULT_CHART_CONFIG.GRID_RIGHT_MULTI_AXIS } : {}),
        ...range,
        ...(axis.name ? { name: axis.name, nameLocation: 'middle' as const, nameGap: 40 } : {}),
        axisLabel: {
          formatter: formatValue,
        },
        axisLine: { show: true },
        axisTick: { show: true },
        splitLine: { show: false },
        zlevel: 1,
      };
    });

    const render = () => {
      const chart = chartInstanceRef.current;
      if (!chart) return;

      const containerWidth = chartRef.current?.clientWidth ?? 0;
      const gridBottom = getGridBottom(legendNames, containerWidth);
      const gridRight =
        yAxisList.length > 1
          ? DEFAULT_CHART_CONFIG.GRID_RIGHT_MULTI_AXIS
          : DEFAULT_CHART_CONFIG.GRID_RIGHT;

      chart.setOption(
        {
          animation: false,
          legend: {
            show: legendShow,
            data: legendNames,
            bottom: 0,
            icon: 'square',
            itemWidth: LEGEND_ICON_WIDTH,
            itemHeight: 10,
            itemGap: LEGEND_ITEM_GAP,
          },
          grid: {
            top: DEFAULT_CHART_CONFIG.GRID_TOP,
            bottom: gridBottom,
            right: gridRight,
            left: DEFAULT_CHART_CONFIG.GRID_LEFT,
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
          yAxis: yAxisEChartsOption,
          tooltip: {
            show: true,
            trigger: 'axis',
            confine: true,
            formatter:
              tooltipFormatter ??
              ((params: unknown) => formatAxisTooltip(params, getFormat(yAxisList[0].unit))),
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
        // agent/시간/지표 변경으로 series 나 y축 수가 줄어도 이전 것이 잔존하지 않도록 항상 교체한다.
        { replaceMerge: ['series', 'yAxis'] },
      );
    };

    renderRef.current = render;
    render();
  }, [
    data,
    seriesOptions,
    yAxis,
    legendShow,
    tooltipFormatter,
    emptyMessage,
    chartInstanceRef,
    chartRef,
    renderRef,
  ]);

  return (
    <div
      style={style}
      className={cn('w-full h-full min-h-0 overflow-hidden', className)}
      ref={chartRef}
    />
  );
};
