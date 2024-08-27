import React from 'react';
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
import { type ChartConfig, ChartContainer } from '../../ui/chart';
import {
  OpenTelemetryChartCommon,
  OpenTelemetryChartCommonProps,
} from './OpenTelemetryChartCommon';

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
const DEFAULT_CHART_TYPE = 'line';
export interface OpenTelemetryChartProps extends OpenTelemetryChartCommonProps {
  chartType: string;
  chartData: {
    [key: string]: number;
  }[];
  chartDataConfig: ChartConfig;
  dashboardId?: string;
}
export const OpenTelemetryChart = ({
  chartType = DEFAULT_CHART_TYPE,
  chartData,
  chartDataConfig,
  dashboardId,
  ...chartCommonProps
}: OpenTelemetryChartProps) => {
  const dataKeys = Object.keys(chartDataConfig);
  const { ChartComponent, ChartChildComponent, chartChildProps } =
    CHART_DEFINITION[chartType as keyof typeof CHART_DEFINITION] ||
    CHART_DEFINITION[DEFAULT_CHART_TYPE];

  return (
    <ChartContainer config={chartDataConfig} className="w-full h-full p-1.5 aspect-auto">
      <ChartComponent
        accessibilityLayer
        data={chartData}
        margin={{ right: 12 }}
        syncId={dashboardId}
      >
        {OpenTelemetryChartCommon(chartCommonProps)}
        {dataKeys.map((key) => (
          <ChartChildComponent
            key={key}
            dataKey={key}
            fill={`var(--color-${key})`}
            stroke={`var(--color-${key})`}
            {...chartChildProps}
          />
        ))}
      </ChartComponent>
    </ChartContainer>
  );
};
