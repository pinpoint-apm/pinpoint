import { useGetInspectorAgentDataSourceChartData } from '@pinpoint-fe/ui/hooks';
import { InspectorChart } from '../InspectorChart';
import { useDataSourceChartConfig } from '../../../../lib';

export interface AgentDataSourceChartProps {
  className?: string;
  emptyMessage?: string;
}

export const AgentDataSourceChart = ({ ...props }: AgentDataSourceChartProps) => {
  const { data } = useGetInspectorAgentDataSourceChartData({ metricDefinitionId: 'dataSource' });
  const chartConfig = useDataSourceChartConfig(data);

  return (
    chartConfig && (
      <InspectorChart
        data={chartConfig.chartData}
        chartOptions={chartConfig.chartOptions}
        {...props}
      >
        <div id="dataSourceTooltip" className="w-full px-2"></div>
      </InspectorChart>
    )
  );
};
