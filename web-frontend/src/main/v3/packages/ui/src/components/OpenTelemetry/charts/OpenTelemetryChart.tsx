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
import { toCssVariable } from '../../../lib/charts/util';

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

export type ChartDataConfig = ChartConfig & {
  [key: string]: {
    stack?: boolean;
  };
};
export interface OpenTelemetryChartProps extends OpenTelemetryChartCommonProps {
  chartType: string;
  chartData: {
    [key: string]: number;
  }[];
  chartDataConfig: ChartDataConfig;
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
  const uniqueId = React.useId();
  const getStackId = (dataKey: string) => {
    return chartDataConfig[dataKey].stack ? uniqueId : undefined;
  };

  const chartContainerRef = React.useRef<HTMLDivElement>(null);

  return (
    <ChartContainer
      config={chartDataConfig}
      className="w-full h-full p-1.5 aspect-auto"
      ref={chartContainerRef}
    >
      <ChartComponent
        accessibilityLayer
        data={chartData}
        margin={{ right: 12 }}
        syncId={dashboardId}
      >
        {OpenTelemetryChartCommon(chartCommonProps, chartContainerRef)}
        {dataKeys.map((key) => (
          <ChartChildComponent
            key={key}
            dataKey={key}
            stackId={getStackId(key)}
            fill={`var(--color-${toCssVariable(key)})`}
            stroke={`var(--color-${toCssVariable(key)})`}
            {...chartChildProps}
          />
        ))}
      </ChartComponent>
    </ChartContainer>
  );
};
