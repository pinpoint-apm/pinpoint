import { colors } from '@pinpoint-fe/ui/src/constants';
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
} from '../../../components/ui';
import { XAxis, YAxis, LabelList, Bar, BarChart } from 'recharts';

export type ChartData = {
  vmVersion?: string;
  agentVersion?: string;
  value: number;
};

export function AgentStatisticChart({
  type,
  chartData,
}: {
  type: 'vmVersion' | 'agentVersion';
  chartData?: ChartData[];
}) {
  const chartConfig = {
    value: {
      label: type === 'vmVersion' ? 'JVM' : 'Agent',
      color: type === 'vmVersion' ? colors.blue[600] : colors.orange[500],
    },
  } satisfies ChartConfig;

  return (
    <ChartContainer config={chartConfig} className="w-full h-full">
      <BarChart
        accessibilityLayer
        data={chartData}
        layout="vertical"
        margin={{
          right: 50,
          left: 15,
        }}
      >
        <XAxis type="number" dataKey={'value'} />
        <YAxis
          dataKey={type}
          type="category"
          width={type === 'vmVersion' ? 60 : 120}
          tickLine={false}
        />
        <ChartLegend content={<ChartLegendContent />} />
        <Bar dataKey="value" fill="var(--color-value)">
          <LabelList
            dataKey="value"
            position="right"
            offset={8}
            className="fill-foreground"
            fontSize={12}
          />
        </Bar>
      </BarChart>
    </ChartContainer>
  );
}
