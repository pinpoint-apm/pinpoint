import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart, LineChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, GraphicComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { Chart } from '@pinpoint-fe/ui/src/constants';
import { getFormat, escapeHTMLEntities } from '@pinpoint-fe/ui/src/utils';
import { CHART_SERIES_COLORS, getRandomColorInHSL } from '../../../lib/colors';
import { useEChartsInstance } from '../../../lib/charts/useEChartsInstance';
import { buildEmptyMessageGraphic } from '../../../lib/charts/echartsCommonOptions';
import { defaultTickFormatter } from '../../../lib/charts/echartsTimeSeriesFormat';
import { Tooltip, TooltipContent, TooltipPortal, TooltipProvider, TooltipTrigger } from '../../ui';

echarts.use([
  BarChart,
  LineChart,
  GridComponent,
  TooltipComponent,
  GraphicComponent,
  CanvasRenderer,
]);

// tooltip 합계(total) 행 색상. (recharts 버전과 동일하게 팔레트의 마지막 색을 쓴다)
const TOTAL_DATA_KEY = 'total';
const TOTAL_DATA_COLOR = CHART_SERIES_COLORS[CHART_SERIES_COLORS.length - 1];

export interface OpenTelemetryMetricChartProps {
  syncId?: string | number;
  chartData: Chart;
  unit?: string;
  tooltipConfig?: { showTotal?: boolean };
  isAnimationActive?: boolean;
  xAxisTickFormatter?: (value: number) => string;
  yAxisTickFormatter?: (value: number) => string;
}

interface SeriesMeta {
  name: string;
  color: string;
  chartType?: string;
  values: number[];
}

// 시리즈 이름이 길면 말줄임(...) 처리한다. 값(value)은 항상 온전히 보이도록 이름 영역만 줄인다.
const tooltipRow = (color: string | undefined, name: string, value: string, nameMaxWidth: number) =>
  `<div style="display: flex; justify-content: space-between; gap: 12px; align-items: center;">
     <div style="display: flex; gap: 5px; align-items: center; min-width: 0;">
       <div style="width: 10px; height: 10px; background: ${color}; flex-shrink: 0;"></div>
       <span style="display: inline-block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: ${nameMaxWidth}px; vertical-align: middle;">${escapeHTMLEntities(name)}</span>
     </div>
     <div style="flex-shrink: 0;">${escapeHTMLEntities(value)}</div>
   </div>`;

export const OpenTelemetryMetricChart = ({
  syncId,
  chartData,
  unit = '',
  tooltipConfig,
  isAnimationActive = true,
  xAxisTickFormatter,
  yAxisTickFormatter,
}: OpenTelemetryMetricChartProps) => {
  const group = syncId != null ? String(syncId) : undefined;
  const { chartRef, chartInstanceRef, renderRef } = useEChartsInstance({ group });
  const showTotal = tooltipConfig?.showTotal;

  // metricValueGroups 안의 metricValue 하나가 시리즈 하나. 색상은 그룹 내 index 기준.
  // echarts series 와 커스텀 범례가 동일한 이름/색을 쓰도록 한 곳에서 만든다.
  const seriesMeta = React.useMemo<SeriesMeta[]>(
    () =>
      (chartData?.metricValueGroups ?? []).flatMap((mvg) =>
        (mvg?.metricValues ?? []).map((mv, mvi) => ({
          name: mv?.fieldName || '',
          color: CHART_SERIES_COLORS[mvi] ?? getRandomColorInHSL(),
          chartType: mvg?.chartType,
          values: mv?.values ?? [],
        })),
      ),
    [chartData],
  );

  React.useEffect(() => {
    if (!chartInstanceRef.current) return;

    const timestamps = chartData?.timestamp ?? [];
    const formatValue = yAxisTickFormatter ?? getFormat(unit || '');

    const series = seriesMeta.map(({ name, color, chartType, values }) => {
      // -1(미수집)/null 은 선을 끊기 위해 null 로 둔다.
      const data = timestamps.map((_, i) => {
        const raw = values?.[i];
        return raw === -1 || raw == null ? null : raw;
      });

      return {
        name,
        type: chartType === 'bar' ? ('bar' as const) : ('line' as const),
        data,
        showSymbol: false,
        smooth: chartType !== 'bar',
        lineStyle: { width: 1 },
        areaStyle: chartType === 'area' ? { opacity: 0.4 } : undefined,
        itemStyle: { color },
        // 그래프/범례 항목에 hover 하면 다른 시리즈를 흐리게 해 강조한다.
        emphasis: { focus: 'series' as const },
      };
    });

    const hasData = timestamps.length > 0 && series.some((s) => s.data.some((v) => v != null));

    const tooltipFormatter = (
      // echarts 툴팁 params 는 라이브러리 타입이 느슨해 any 로 받는다.
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      params: any,
    ) => {
      if (!Array.isArray(params) || params.length === 0) return '';
      const axisValue = Number(params[0].axisValue);
      const header = defaultTickFormatter(axisValue).replace(/\n/g, ' ');

      // 시리즈 이름 말줄임 폭: 차트 폭의 60% 정도까지만 이름에 쓰고, 나머지는 값 표시에 남긴다.
      const chartWidth = chartRef.current?.offsetWidth || 392;
      const nameMaxWidth = Math.max(80, Math.round(chartWidth * 0.6));

      let total = 0;
      const rows = params
        .map((p: { value?: number | null; seriesName?: string; color?: string }) => {
          const v = typeof p.value === 'number' ? p.value : null;
          if (v == null) return null;
          total += v;
          return tooltipRow(p.color, String(p.seriesName ?? ''), formatValue(v), nameMaxWidth);
        })
        .filter(Boolean);
      if (rows.length === 0) return '';
      if (showTotal) {
        rows.push(tooltipRow(TOTAL_DATA_COLOR, TOTAL_DATA_KEY, formatValue(total), nameMaxWidth));
      }
      return `<div>
                <div style="margin-bottom: 5px;"><strong>${escapeHTMLEntities(header)}</strong></div>
                ${rows.join('')}
              </div>`;
    };

    const render = () => {
      const chart = chartInstanceRef.current;
      if (!chart) return;

      const chartWidth = chartRef.current?.offsetWidth || 392;

      // y축 눈금 숫자 폭을 추정해 왼쪽 여백과 단위 이름 간격을 잡는다. 숫자가 길어도(예: 6000000000000)
      // 왼쪽 세로 단위 이름이 숫자와 겹치지 않도록 nameGap 을 숫자 폭보다 크게 준다. (12px 폰트 ≈ 글자당 7px)
      const numericValues = series.flatMap((s) => s.data).filter((v): v is number => v != null);
      const maxAbs = numericValues.length ? Math.max(...numericValues.map((v) => Math.abs(v))) : 0;
      const estLabelWidth = Math.max(String(formatValue(maxAbs)).length * 7, 14);
      const yNameGap = estLabelWidth + 6;
      const gridLeft = unit ? estLabelWidth + 24 : estLabelWidth + 6;

      // 범례는 차트 아래 별도 React 요소로 그리므로, grid.bottom 은 x축 축선 아래 높이만 예약한다.
      // (tick 5 + 라벨 margin 8 + 라벨 높이: 오늘=1줄 16 / 그 외=2줄 30)
      const sampleTs = timestamps[0];
      const sampleLabel =
        sampleTs != null
          ? xAxisTickFormatter
            ? xAxisTickFormatter(Number(sampleTs))
            : defaultTickFormatter(Number(sampleTs))
          : '';
      const gridBottom = (String(sampleLabel).includes('\n') ? 30 : 16) + 13;

      chart.setOption(
        {
          animation: isAnimationActive,
          grid: {
            top: 20,
            right: 30,
            bottom: gridBottom,
            left: gridLeft,
          },
          xAxis: {
            type: 'category',
            data: timestamps,
            boundaryGap: series.some((s) => s.type === 'bar'),
            axisLine: { show: true },
            axisTick: { show: true, alignWithLabel: true },
            axisLabel: {
              formatter: (value: string | number) =>
                xAxisTickFormatter
                  ? xAxisTickFormatter(Number(value))
                  : defaultTickFormatter(Number(value)),
            },
          },
          yAxis: {
            type: 'value',
            // y축 축선과 tick 은 표시하되, 가로 격자선은 그리지 않는다.
            axisLine: { show: true },
            axisTick: { show: true },
            splitLine: { show: false },
            // 단위 이름은 (기존 recharts 처럼) y축 왼쪽에 세로로 둔다.
            ...(unit
              ? {
                  name: unit,
                  nameLocation: 'middle' as const,
                  nameGap: yNameGap,
                  nameRotate: 90,
                  nameTextStyle: { fontSize: 12 },
                }
              : {}),
            axisLabel: { formatter: (value: number) => formatValue(value) },
          },
          tooltip: {
            trigger: 'axis',
            confine: true,
            formatter: tooltipFormatter,
            extraCssText: `max-width: ${chartWidth}px;`,
          },
          series,
          graphic: buildEmptyMessageGraphic(hasData, 'No Data'),
        },
        { replaceMerge: ['series'] },
      );
    };

    renderRef.current = render;
    render();
  }, [
    chartData,
    seriesMeta,
    unit,
    showTotal,
    isAnimationActive,
    xAxisTickFormatter,
    yAxisTickFormatter,
    chartInstanceRef,
    chartRef,
    renderRef,
  ]);

  // 범례 항목에 hover 하면 해당 시리즈를 강조(다른 시리즈는 흐려짐)한다. (recharts legend hover dimming 대체)
  const highlightSeries = (name: string, on: boolean) =>
    chartInstanceRef.current?.dispatchAction({
      type: on ? 'highlight' : 'downplay',
      seriesName: name,
    });

  return (
    <div className="flex flex-col w-full h-full min-w-0 min-h-0 overflow-hidden">
      <div ref={chartRef} className="flex-1 min-h-0" />
      {seriesMeta.length > 0 && (
        <TooltipProvider delayDuration={100}>
          <div className="flex flex-wrap justify-center items-center gap-x-3 gap-y-0.5 px-2 pb-1 shrink-0">
            {seriesMeta.map((s, i) => (
              <Tooltip key={`${s.name}-${i}`}>
                <TooltipTrigger asChild>
                  {/* 키보드 사용자도 hover 와 동일하게 강조/전체이름 tooltip 을 쓸 수 있도록 focus 가능한 button 으로 둔다. */}
                  <button
                    type="button"
                    className="inline-flex items-center max-w-full min-w-0 gap-1 cursor-default border-0 bg-transparent p-0"
                    onMouseEnter={() => highlightSeries(s.name, true)}
                    onMouseLeave={() => highlightSeries(s.name, false)}
                    onFocus={() => highlightSeries(s.name, true)}
                    onBlur={() => highlightSeries(s.name, false)}
                  >
                    <span
                      className="h-2 w-2 shrink-0 rounded-[2px]"
                      style={{ backgroundColor: s.color }}
                    />
                    <span className="text-xs truncate text-muted-foreground">{s.name}</span>
                  </button>
                </TooltipTrigger>
                <TooltipPortal>
                  <TooltipContent>
                    <p className="max-w-[80vw] break-all">{s.name}</p>
                  </TooltipContent>
                </TooltipPortal>
              </Tooltip>
            ))}
          </div>
        </TooltipProvider>
      )}
    </div>
  );
};
