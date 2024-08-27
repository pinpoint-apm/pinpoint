import { format } from 'date-fns';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../ui/card';
import { type ChartConfig } from '../../ui/chart';
import { useAtomValue } from 'jotai';
import { userMetricConfigAtom } from '@pinpoint-fe/atoms';
import { getFormat } from '@pinpoint-fe/utils';
import { COLORS } from './constant';
import { OpenTelemetryChart } from './OpenTelemetryChart';

const now = new Date();
const duration = 300 * 1000; // 5min
const tickCount = 5;

const chartConfig = {
  value: {
    label: 'Sample Value',
    color: COLORS[1],
  },
} satisfies ChartConfig;

export interface PreviewChartProps {}

export const PreviewChart = () => {
  const { chartType = 'line', unit = '', title } = useAtomValue(userMetricConfigAtom);
  const chartData = Array(tickCount)
    .fill(now)
    .map((now, i) => {
      return {
        timestamp: now - (duration / (tickCount - 1)) * i,
        value: Math.floor(Math.random() * 100),
      };
    })
    .reverse();

  return (
    <Card className="border-none rounded-none shadow-none">
      <CardHeader>
        <CardTitle className="font-medium">Metric Preview</CardTitle>
        <CardDescription>{title}</CardDescription>
      </CardHeader>
      <CardContent className="w-full h-48 px-0">
        <OpenTelemetryChart
          chartType={chartType}
          chartData={chartData}
          chartDataConfig={chartConfig}
          xAxisConfig={{
            dataKey: 'timestamp',
            tickFormatter: (value) => `${format(value, 'HH:mm')}`,
          }}
          yAxisConfig={{
            tickFormatter: (value) => getFormat(unit)(value),
            label: {
              value: unit,
              position: 'insideLeft',
              angle: -90,
              style: { fontSize: '0.75rem' },
            },
          }}
        />
      </CardContent>
    </Card>
  );
};
