import { formatInTimeZone } from 'date-fns-tz';
import { useAtomValue } from 'jotai';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../ui/card';
import { userMetricConfigAtom } from '@pinpoint-fe/ui/src/atoms';
import { Chart } from '@pinpoint-fe/ui/src/constants';
import { useTimezone } from '@pinpoint-fe/ui/src/hooks';
import { OpenTelemetryMetricChart } from './OpenTelemetryMetricChart';

const now = new Date();
const duration = 300 * 1000; // 5min
const tickCount = 5;

export interface PreviewChartProps {}

export const PreviewChart = () => {
  const { chartType = 'line', unit = '', title } = useAtomValue(userMetricConfigAtom);
  const [timezone] = useTimezone();

  const samples = Array(tickCount)
    .fill(now)
    .map((n: Date, i) => ({
      timestamp: +n - (duration / (tickCount - 1)) * i,
      value: Math.floor(Math.random() * 100),
    }))
    .reverse();

  // 미리보기 샘플을 실제 메트릭 차트와 동일한 Chart 형태로 만들어 OpenTelemetryMetricChart 로 렌더한다.
  const chartData: Chart = {
    title: '',
    timestamp: samples.map((s) => s.timestamp),
    metricValueGroups: [
      {
        groupName: '',
        chartType,
        unit,
        metricValues: [{ fieldName: 'Sample Value', values: samples.map((s) => s.value) }],
      },
    ],
  };

  return (
    <Card className="border-none rounded-none shadow-none">
      <CardHeader>
        <CardTitle className="font-medium">Metric Preview</CardTitle>
        <CardDescription>{title}</CardDescription>
      </CardHeader>
      <CardContent className="w-full h-48 px-6">
        <OpenTelemetryMetricChart
          chartData={chartData}
          unit={unit}
          xAxisTickFormatter={(value) => formatInTimeZone(value, timezone, 'HH:mm')}
        />
      </CardContent>
    </Card>
  );
};
