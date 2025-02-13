import { InspectorAgentChart, InspectorApplicationChart } from '@pinpoint-fe/ui/src/constants';
import { ChartOptions, Data } from 'billboard.js';
import { getFormat } from '@pinpoint-fe/ui/src/utils';
import { useChartAxis } from './useChartAxis';
import { useChartParseData } from './useChartParseData';
import { TOOLTIP_NOT_LINKED_CHART_LIST, useChartTooltip } from './useChartTooltip';
import { useChartType } from './useChartType';

export const DEFAULT_CHART_CONFIG = {
  PADDING_TOP: 20,
  PADDING_BOTTOM: 10,
  PADDING_RIGHT: 30,
  PADDING_LEFT: 15,
  EXTRA_AXIS_PADDING_RIGHT: 40,
  DEFAULT_MAX: 10,
  X_AXIS_TICK_COUNT: 4,
} as const;

export const useChartConfig = (
  data?: InspectorAgentChart.Response | InspectorApplicationChart.Response,
  chartInitOptions?: {
    dataOptions?: Data;
    elseOptions?: ChartOptions;
  },
) => {
  const { chartMetricData, chartTooltipData, dataKeys } = useChartParseData(data?.metricValues);
  const { chartUnitAxisInfo, getDataAxis } = useChartAxis();
  const { getTooltipData, getTooltipStr } = useChartTooltip(chartTooltipData);
  const { getChartType } = useChartType();

  if (!data) return null;

  const chartData = {
    title: data.title,
    timestamp: data.timestamp,
    metricValues: chartMetricData,
  };

  const chartDataOptionsFromData = chartData.metricValues.reduce(
    (acc, { fieldName, chartType, unit }) => {
      return {
        types: {
          ...acc.types,
          [fieldName]: getChartType(chartType),
        },
        axes: {
          ...acc.axes,
          [fieldName]: getDataAxis(unit),
        },
      };
    },
    {
      types: {},
      axes: {},
    },
  );
  const chartDataOptions = { ...chartDataOptionsFromData, ...chartInitOptions?.dataOptions };
  const chartOptions: ChartOptions = {
    data: {
      ...chartDataOptions,
      groups: [dataKeys],
      order: null,
    },
    padding: {
      right:
        Object.values(chartUnitAxisInfo).length > 1
          ? DEFAULT_CHART_CONFIG.EXTRA_AXIS_PADDING_RIGHT
          : DEFAULT_CHART_CONFIG.PADDING_RIGHT,
    },
    axis: Object.entries(chartUnitAxisInfo).reduce((acc, [unit, axisName]) => {
      return {
        ...acc,
        [axisName]: {
          tick: {
            format: getFormat(unit),
          },
          show: true,
          padding: {
            bottom: 0,
          },
          min: 0,
          default: [0, 10],
        },
      };
    }, {}),
    tooltip: {
      linked: !TOOLTIP_NOT_LINKED_CHART_LIST.includes(chartData.title),
      contents:
        chartTooltipData.length !== 0
          ? (d, defaultTitleFormat, defaultValueFormat, color) => {
              const focusIndex = d[0].index;
              const title = defaultTitleFormat(d[0].x) as string;
              const originalData = d.map(({ id, value }) => {
                return {
                  id,
                  value: defaultValueFormat(value!, undefined, id),
                  color: color(id),
                };
              });
              const contentsData = [...originalData, ...getTooltipData(focusIndex)!];

              return getTooltipStr(title, contentsData);
            }
          : undefined,
    },
    ...chartInitOptions?.elseOptions,
  };

  return { chartData, chartOptions };
};
