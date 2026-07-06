import { useGetInspectorAgentDataSourceChartData } from '@pinpoint-fe/ui/src/hooks';
import { InspectorChart } from '../InspectorChart';
import { DATA_SOURCE_TOOLTIP_ID, useDataSourceChartConfig } from '../../../../lib';

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
        <div id={DATA_SOURCE_TOOLTIP_ID} className="w-full px-2 mt-3"></div>
      </InspectorChart>
    )
  );
};
