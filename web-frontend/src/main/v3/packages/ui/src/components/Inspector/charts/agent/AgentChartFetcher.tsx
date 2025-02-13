import { useGetInspectorAgentChartData } from '@pinpoint-fe/ui/src/hooks';
import { useChartConfig } from '../../../../lib';
import { ChartCore, ChartCoreProps } from '../ChartCore';

export const AGENT_CHART_ID_LIST = [
  'heap',
  'nonHeap',
  'cpu',
  'apdex',
  'transaction',
  'activeTrace',
  'totalThreadCount',
  'responseTime',
  'fileDescriptor',
  'directCount',
  'directMemoryUsed',
  'mappedMemoryCount',
  'mappedMemoryUsed',
  'loadedClass',
  'unloadedClass',
] as const;

export type AGENT_CHART_ID = (typeof AGENT_CHART_ID_LIST)[number];

export interface AgentChartFetcherProps extends Omit<ChartCoreProps, 'chartOptions' | 'data'> {
  chartId: AGENT_CHART_ID;
  fromDate?: Date;
  toDate?: Date;
  agentId?: string;
  children?: (props: ChartCoreProps) => React.ReactNode;
}

export const AgentChartFetcher = ({
  chartId,
  fromDate,
  toDate,
  agentId,
  children,
  ...props
}: AgentChartFetcherProps) => {
  const { data } = useGetInspectorAgentChartData({
    metricDefinitionId: chartId,
    fromDate,
    toDate,
    agentId,
  });
  const chartConfig = useChartConfig(data);

  if (chartConfig) {
    return typeof children === 'function' && data ? (
      children({ data: chartConfig.chartData, chartOptions: chartConfig.chartOptions, ...props })
    ) : (
      <ChartCore data={chartConfig.chartData} chartOptions={chartConfig.chartOptions} {...props} />
    );
  } else {
    return null;
  }
};
