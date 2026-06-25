import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart as BarChartEcharts, LineChart as LineChartEcharts } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { formatNewLinedDateString } from '@pinpoint-fe/ui/src/utils';
import { isValid } from 'date-fns';
import { UrlStatChartType as UrlStatChartApi } from '@pinpoint-fe/ui/src/constants';
import { cn } from '../../../lib';

echarts.use([
  BarChartEcharts,
  LineChartEcharts,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  CanvasRenderer,
]);

// legend가 차트 폭에 따라 몇 줄로 줄바꿈되는지 계산해 grid.bottom을 동적으로 잡기 위한 상수/헬퍼.
// 이렇게 해야 legend를 하단에 둔 채로 2줄짜리 x축 라벨과 겹치지 않는다.
const LEGEND_FONT = '12px sans-serif'; // echarts legend 기본 폰트
const LEGEND_ICON_WIDTH = 10; // legend.itemWidth
const LEGEND_ICON_TEXT_GAP = 5; // 아이콘과 텍스트 사이 기본 간격
const LEGEND_ITEM_GAP = 15; // legend.itemGap
const LEGEND_PADDING = 5; // legend 기본 좌우 padding
const LEGEND_ROW_HEIGHT = 20; // legend 한 줄 높이
const X_AXIS_LABEL_HEIGHT = 38; // x축 2줄(날짜/시간) 라벨 높이 + 여백
const BOTTOM_GAP = 12; // x축 라벨과 legend 사이 간격

const measureCanvas = typeof document !== 'undefined' ? document.createElement('canvas') : null;
const measureCtx = measureCanvas?.getContext('2d') ?? null;

const getLegendRowCount = (names: string[], availableWidth: number) => {
  if (!measureCtx || availableWidth <= 0 || names.length === 0) return 1;
  measureCtx.font = LEGEND_FONT;

  let rows = 1;
  let lineWidth = 0;
  for (const name of names) {
    const textWidth = measureCtx.measureText(name).width;
    const itemWidth = LEGEND_ICON_WIDTH + LEGEND_ICON_TEXT_GAP + textWidth;
    const nextWidth = lineWidth === 0 ? itemWidth : lineWidth + LEGEND_ITEM_GAP + itemWidth;
    if (nextWidth > availableWidth && lineWidth > 0) {
      rows += 1;
      lineWidth = itemWidth;
    } else {
      lineWidth = nextWidth;
    }
  }
  return rows;
};

const getGridBottom = (names: string[], containerWidth: number) => {
  const availableWidth = containerWidth - LEGEND_PADDING * 2;
  const rows = getLegendRowCount(names, availableWidth);
  return X_AXIS_LABEL_HEIGHT + BOTTOM_GAP + rows * LEGEND_ROW_HEIGHT;
};

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
  const chartRef = React.useRef<HTMLDivElement>(null);
  const chartInstanceRef = React.useRef<echarts.EChartsType | null>(null);
  // 데이터 effect에서 만든 렌더 함수를 보관해, resize 시 폭에 맞춰 grid.bottom을 다시 계산한다.
  const renderRef = React.useRef<(() => void) | null>(null);

  React.useEffect(() => {
    if (!chartRef.current) return;

    const chart = echarts.init(chartRef.current);
    chartInstanceRef.current = chart;

    const wrapperElement = chartRef.current;
    const resizeObserver = new ResizeObserver(() => {
      chart.resize();
      // 폭이 바뀌면 legend 줄바꿈 행 수가 달라지므로 grid.bottom을 다시 계산한다.
      renderRef.current?.();
    });
    resizeObserver.observe(wrapperElement);

    return () => {
      resizeObserver.disconnect();
      chart.dispose();
      chartInstanceRef.current = null;
    };
  }, []);

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
              formatter: (value: number | string) => {
                const ts = typeof value === 'string' ? Number(value) : value;
                const date = new Date(ts);
                if (isValid(date)) {
                  return formatNewLinedDateString(date);
                }
                return String(value);
              },
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
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            formatter: (params: any) => {
              if (!Array.isArray(params) || params.length === 0) return '';
              const firstParam = params[0];
              const axisValue = firstParam.axisValue;
              const axisValueNum = typeof axisValue === 'number' ? axisValue : Number(axisValue);
              const dateStr = isValid(new Date(axisValueNum))
                ? formatNewLinedDateString(axisValueNum).replace('\n', ' ')
                : String(axisValue);
              const rows = params
                .map(
                  (param: {
                    value?: number | [number, number];
                    seriesName?: string;
                    color?: string;
                  }) => {
                    const yValue = typeof param.value === 'number' ? param.value : param.value?.[1];
                    if (yValue == null) return null;
                    return `<div style="display: flex; justify-content: space-between; gap: 12px; align-items: center;">
                  <div style="display: flex; gap: 5px; align-items: center;">
                    <div style="width: 10px; height: 10px; background: ${param.color};"></div>${param.seriesName}
                  </div>
                  <div>${formatter ? formatter(yValue) : String(yValue)}</div>
                </div>`;
                  },
                )
                .filter(Boolean)
                .join('');
              return `<div>
            <div style="margin-bottom: 5px;"><strong>${dateStr}</strong></div>
            ${rows}
          </div>`;
            },
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
  }, [data, emptyMessage, chartType, yAxisName, chartColors, formatter]);

  return <div className={cn('w-full h-full', className)} ref={chartRef} />;
};
