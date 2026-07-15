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
import { Chart } from '@pinpoint-fe/ui/src/constants';
import { isNil } from 'lodash';
import { useState, useEffect } from 'react';
import { ChartConfig } from '../../components/ui';
import { CHART_SERIES_COLORS, getRandomColorInHSL } from '../../lib/colors';

export const COLORS = CHART_SERIES_COLORS;

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
            label: fieldName,
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

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  function renderChartChildComponents(customChartChildProps?: (config: any) => any) {
    return Object.keys(chartConfig)?.map((configKey) => {
      const config = chartConfig?.[configKey];
      const { ChartChildComponent, chartChildProps } =
        CHART_DEFINITION[config?.chartType as keyof typeof CHART_DEFINITION] ||
        CHART_DEFINITION['line'];

      return (
        <ChartChildComponent
          key={configKey}
          dataKey={configKey}
          fill={config?.color}
          stroke={config?.color}
          {...chartChildProps}
          {...customChartChildProps?.(config)}
        />
      );
    });
  }

  return { data, chartConfig, maxValue, renderChartChildComponents };
}
