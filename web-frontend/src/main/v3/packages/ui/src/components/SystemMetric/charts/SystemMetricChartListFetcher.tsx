import { useGetSystemMetricMetricInfoData } from '@pinpoint-fe/ui/src/hooks';
import { SystemMetricChart } from './SystemMetricChart';

export interface SystemMetricChartListFetcherProps {
  emptyMessage?: string;
}

export const SystemMetricChartListFetcher = ({
  emptyMessage,
}: SystemMetricChartListFetcherProps) => {
  const { data } = useGetSystemMetricMetricInfoData();

  return (
    <div className="grid md:grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
      {data &&
        data.map((chartInfo, i) => (
          <SystemMetricChart
            key={i}
            chartInfo={chartInfo}
            emptyMessage={emptyMessage}
            className="aspect-video min-h-0"
          />
        ))}
    </div>
  );
};
