import { ChartOptions } from 'billboard.js';
import { InspectorAgentDataSourceChart } from '@pinpoint-fe/constants';
import { useChartAxis } from './useChartAxis';
import { useChartType } from './useChartType';
import { DEFAULT_CHART_CONFIG } from './useChartConfig';
import { getFormat } from '@pinpoint-fe/utils';
import { useDataSourceChartTooltip } from './useDataSourceChartTooltip';

export const useDataSourceChartConfig = (data?: InspectorAgentDataSourceChart.Response) => {
  const { chartUnitAxisInfo, getDataAxis } = useChartAxis();
  const { getTooltipData, getTooltipStr, tooltipTitleList } = useDataSourceChartTooltip(
    data?.metricValueGroups,
  );
  const { getChartType } = useChartType();

  if (!data) return null;

  const chartData = {
    title: data.title,
    timestamp: data.timestamp,
    metricValues: data.metricValueGroups.map(({ metricValues, tags }, index) => {
      const newFieldName =
        tags.find(({ name }) => name === 'databaseName')?.value || `Database ${index}`;

      return { ...metricValues[0], fieldName: newFieldName };
    }),
  };

  const chartDataOptions = chartData.metricValues.reduce(
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

  const chartOptions: ChartOptions = {
    data: chartDataOptions,
    axis: Object.entries(chartUnitAxisInfo).reduce((acc, [unit, axisName]) => {
      return {
        ...acc,
        [axisName]: {
          label: {
            text: 'Active Avg',
            position: 'outer-middle',
          },
          tick: {
            format: getFormat(unit),
          },
          show: true,
          padding: {
            bottom: 0,
          },
          min: 0,
          default: [0, DEFAULT_CHART_CONFIG.DEFAULT_MAX],
        },
      };
    }, {}),
    legend: {
      show: false,
    },
    tooltip: {
      contents: (d, defaultTitleFormat, _, color) => {
        const focusIndex = d[0].index;
        const titleList = [defaultTitleFormat(d[0].x) as string, ...tooltipTitleList];
        const tooltipData = d.map(({ id }) => {
          return {
            id,
            values: getTooltipData(focusIndex),
            color: color(id),
          };
        });
        const tooltipStr = getTooltipStr(titleList, tooltipData);
        document.querySelector('#dataSourceTooltip')!.innerHTML = tooltipStr;
        return '';
      },
    },
  };

  return { chartData, chartOptions };
};
