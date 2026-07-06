import { InspectorAgentChart, InspectorApplicationChart } from '@pinpoint-fe/ui/src/constants';
import { useChartParseData } from './useChartParseData';
import {
  AxisTooltipParam,
  createChartTooltipFormatter,
  TOOLTIP_NOT_LINKED_CHART_LIST,
} from './useChartTooltip';
import { EChartsSeriesTypeOption, useChartType } from './useChartType';

export const DEFAULT_CHART_CONFIG = {
  DEFAULT_MAX: 10,
  GRID_TOP: 20,
  GRID_RIGHT: 25,
  GRID_RIGHT_MULTI_AXIS: 45,
  GRID_LEFT: 0,
} as const;

// 페이지 내 Inspector 차트들을 하나의 echarts 그룹으로 묶어 tooltip/axisPointer 를 동기화한다.
// (billboard 의 tooltip.linked: true 대체) Apdex Score 등 TOOLTIP_NOT_LINKED_CHART_LIST 의 차트는 제외한다.
export const INSPECTOR_CHART_GROUP = 'inspector-chart';

// 스택 시리즈(fastCount/normalCount 등)를 하나의 스택으로 쌓기 위한 stack id. (billboard 의 data.groups 대체)
export const INSPECTOR_STACK_GROUP = 'inspector-stack';

// 시리즈(fieldName) 하나의 렌더링 옵션. ChartCore 가 echarts series 로 변환한다.
export interface InspectorSeriesOption extends EChartsSeriesTypeOption {
  unit: string;
  color?: string;
  dashed?: boolean; // billboard 의 regions(점선) 대체 → lineStyle.type: 'dashed'
  stack?: string;
  name?: string; // legend/tooltip 표시명 (지정 시 fieldName 대신 사용)
}

export interface InspectorYAxisOption {
  unit: string;
  name?: string;
}

// ChartCore 가 소비하는, 차트 라이브러리에 독립적인 옵션 기술자.
export interface InspectorChartOptions {
  seriesOptions: Record<string, InspectorSeriesOption>;
  yAxis: InspectorYAxisOption[];
  group?: string;
  legendShow: boolean;
  // trigger:'axis' tooltip 포맷터. 값을 반환하면 floating tooltip 으로, 부수효과 후 '' 를 반환하면
  // dataSource 처럼 외부 요소에 직접 렌더링하는 방식으로 동작한다.
  tooltipFormatter?: (params: AxisTooltipParam[]) => string;
}

export interface InspectorChartInitOptions {
  colors?: Record<string, string>; // fieldName -> color
  dashedFields?: string[]; // 점선으로 그릴 fieldName 목록 (billboard regions 대체)
  legendShow?: boolean;
}

export const useChartConfig = (
  data?: InspectorAgentChart.Response | InspectorApplicationChart.Response | null,
  initOptions?: InspectorChartInitOptions,
) => {
  const { chartMetricData, chartTooltipData, dataKeys } = useChartParseData(data?.metricValues);
  const { getChartType } = useChartType();

  if (!data) return null;

  const chartData = {
    title: data.title,
    timestamp: data.timestamp,
    metricValues: chartMetricData,
  };

  // unit 이 처음 등장한 순서대로 y축을 만든다. (첫 unit -> 왼쪽 축, 그 다음 -> 오른쪽 축)
  const yAxis: InspectorYAxisOption[] = [];
  chartMetricData.forEach(({ unit }) => {
    if (!yAxis.some((axis) => axis.unit === unit)) {
      yAxis.push({ unit });
    }
  });

  const seriesOptions = chartMetricData.reduce<Record<string, InspectorSeriesOption>>(
    (acc, { fieldName, chartType, unit }) => {
      acc[fieldName] = {
        ...getChartType(chartType),
        unit,
        color: initOptions?.colors?.[fieldName],
        dashed: initOptions?.dashedFields?.includes(fieldName),
        stack: dataKeys.includes(fieldName) ? INSPECTOR_STACK_GROUP : undefined,
      };
      return acc;
    },
    {},
  );

  const unitByField = chartMetricData.reduce<Record<string, string>>((acc, { fieldName, unit }) => {
    acc[fieldName] = unit;
    return acc;
  }, {});

  const chartOptions: InspectorChartOptions = {
    seriesOptions,
    yAxis,
    group: TOOLTIP_NOT_LINKED_CHART_LIST.includes(chartData.title)
      ? undefined
      : INSPECTOR_CHART_GROUP,
    legendShow: initOptions?.legendShow ?? true,
    tooltipFormatter: createChartTooltipFormatter({
      unitByField,
      tooltipData: chartTooltipData,
    }),
  };

  return { chartData, chartOptions };
};
