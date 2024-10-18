import { Chart } from '@pinpoint-fe/constants';
import {
  BarChart,
  Bar,
  AreaChart,
  Area,
  LineChart,
  Line,
  LineProps,
  BarProps,
  AreaProps,
} from 'recharts';
import { colors } from '../constant/theme';
import { type ChartConfig } from '../components/ui/chart';
import { isNil } from 'lodash';
import { getRandomColorInHSL } from '../lib/colors';
import { useState, useEffect } from 'react';

export const COLORS = [
  colors.blue[600],
  colors.blue[200],
  colors.orange[500],
  colors.orange[200],
  colors.green[600],
  colors.green[300],
  colors.red[600],
  colors.red[300],
  colors.purple[500],
  colors.purple[300],
  colors.stone[700],
  colors.stone[400],
  colors.pink[400],
  colors.pink[200],
  colors.gray[500],
  colors.gray[300],
  colors.lime[500],
  colors.yellow[200],
  colors.cyan[500],
  colors.cyan[200],
];

const CHART_DEFINITION = {
  line: {
    ChartComponent: LineChart,
    ChartChildComponent: Line as React.ComponentType<LineProps>,
    chartChildProps: { type: 'monotone', dot: false },
  },
  bar: {
    ChartComponent: BarChart,
    ChartChildComponent: Bar as React.ComponentType<BarProps>,
    chartChildProps: {},
  },
  area: {
    ChartComponent: AreaChart,
    ChartChildComponent: Area as React.ComponentType<AreaProps>,
    chartChildProps: { type: 'natural', fillOpacity: '0.4' },
  },
} as const;

export interface ChartData {
  timestamp: number;
  [key: string]: number;
}

export type ChartDataConfig = ChartConfig & {
  [key: string]: {
    chartType: string;
  };
};

export function useRechart(chart: Chart) {
  const [data, setData] = useState<ChartData[]>([]);
  const [chartConfig, setChartConfig] = useState<ChartDataConfig>({});
  const [maxValue, setMaxValue] = useState<number>(-1);

  useEffect(() => {
    if (!chart) {
      return;
    }

    const tempData: ChartData[] = [];
    const tempChartConfig: ChartDataConfig = {};
    let tempMaxValue = -1;

    chart.timestamp?.forEach((t, i) => {
      const dataObj: ChartData = { timestamp: t };

      chart.metricValueGroups?.forEach((mvg) => {
        mvg?.metricValues?.forEach((mv, mvi) => {
          const value = (mv?.values?.[i] === -1 ? null : mv?.values?.[i]) as number;
          const fieldName = mv?.fieldName || '';

          if (!isNil(value) && value > tempMaxValue) {
            tempMaxValue = value;
          }
          dataObj[fieldName] = value;
          tempChartConfig[fieldName] = {
            chartType: mvg?.chartType,
            color: COLORS[mvi] ?? getRandomColorInHSL(),
          };
        });
      });
      tempData.push(dataObj);
    });

    setData(tempData);
    setChartConfig(tempChartConfig);
    setMaxValue(tempMaxValue);
  }, [chart]);

  function renderChartChildComponents() {
    return Object.keys(chartConfig)?.map((configKey) => {
      const config = chartConfig[configKey];
      const { ChartChildComponent, chartChildProps } =
        CHART_DEFINITION[config?.chartType as keyof typeof CHART_DEFINITION] ||
        CHART_DEFINITION['line'];

      return (
        <ChartChildComponent
          key={configKey}
          dataKey={configKey}
          fill={chartConfig?.[configKey]?.color}
          stroke={chartConfig?.[configKey]?.color}
          {...chartChildProps}
        />
      );
    });
  }

  return { data, chartConfig, maxValue, renderChartChildComponents };
}
