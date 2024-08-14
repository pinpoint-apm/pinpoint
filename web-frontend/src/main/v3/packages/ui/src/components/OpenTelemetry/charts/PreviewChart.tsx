import {
  Bar,
  BarChart,
  CartesianGrid,
  XAxis,
  YAxis,
  LineChart,
  Line,
  AreaChart,
  Area,
} from 'recharts';
import { format } from 'date-fns';

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../ui/card';
import {
  type ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from '../../ui/chart';
import { useAtomValue } from 'jotai';
import { userMetricConfigAtom } from '@pinpoint-fe/atoms';
const now = new Date();
const duration = 300 * 1000; // 5min
const tickCount = 5;

const chartConfig = {
  views: {
    label: 'Sample View',
  },
  value: {
    label: 'Sample Value',
    color: 'hsl(var(--chart-2))',
  },
} satisfies ChartConfig;

export interface PreviewChartProps {}

export const PreviewChart = () => {
  const { chartType, unit, title } = useAtomValue(userMetricConfigAtom);
  const chartData = Array(tickCount)
    .fill(now)
    .map((now, i) => {
      return {
        timestamp: now - (duration / (tickCount - 1)) * i,
        value: Math.floor(Math.random() * 100),
      };
    })
    .reverse();
  const renderChart = () => {
    if (chartType === 'bar') {
      return (
        <ChartContainer config={chartConfig} className="w-full h-48 aspect-auto">
          <BarChart accessibilityLayer data={chartData} margin={{ right: 12 }}>
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey="timestamp"
              tickMargin={10}
              tickLine={false}
              axisLine={false}
              tickFormatter={(value) => `${format(value, 'HH:mm:ss')}`}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tickMargin={8}
              label={{ value: unit, position: 'insideLeft', angle: -90 }}
            />
            <ChartTooltip
              content={<ChartTooltipContent className="w-36" nameKey="views" hideLabel />}
            />
            <ChartLegend content={<ChartLegendContent />} />
            <Bar dataKey="value" fill="var(--color-value)" radius={8} />
          </BarChart>
        </ChartContainer>
      );
    }

    if (chartType === 'line') {
      return (
        <ChartContainer config={chartConfig} className="w-full h-48 aspect-auto">
          <LineChart accessibilityLayer data={chartData} margin={{ right: 12 }}>
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey="timestamp"
              tickMargin={10}
              tickLine={false}
              axisLine={false}
              tickFormatter={(value) => `${format(value, 'HH:mm:ss')}`}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tickMargin={8}
              label={{ value: unit, position: 'insideLeft', angle: -90 }}
            />
            <ChartTooltip
              content={
                <ChartTooltipContent
                  className="w-36"
                  nameKey="views"
                  labelFormatter={(value) => {
                    console.log(value);
                    return value;
                  }}
                  hideLabel
                />
              }
            />
            <ChartLegend content={<ChartLegendContent />} />
            <Line type={'monotone'} dataKey="value" stroke="var(--color-value)" dot={false} />
          </LineChart>
        </ChartContainer>
      );
    }

    if (chartType === 'area') {
      return (
        <ChartContainer config={chartConfig} className="w-full h-48 aspect-auto">
          <AreaChart
            accessibilityLayer
            data={chartData}
            margin={{
              right: 12,
            }}
          >
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey="timestamp"
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              tickFormatter={(value) => `${format(value, 'HH:mm:ss')}`}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tickMargin={8}
              label={{ value: unit, position: 'insideLeft', angle: -90 }}
            />
            <ChartTooltip
              content={<ChartTooltipContent className="w-36" nameKey="views" hideLabel />}
            />
            <ChartLegend content={<ChartLegendContent />} />
            <Area
              dataKey="value"
              type="natural"
              fill="var(--color-value)"
              fillOpacity={0.4}
              stroke="var(--color-value)"
            />
          </AreaChart>
        </ChartContainer>
      );
    }
  };
  return (
    <Card className="border-none rounded-none shadow-none">
      <CardHeader>
        <CardTitle className="font-medium">Metric Preview</CardTitle>
        <CardDescription>{title}</CardDescription>
      </CardHeader>
      <CardContent>{renderChart()}</CardContent>
    </Card>
  );
};
