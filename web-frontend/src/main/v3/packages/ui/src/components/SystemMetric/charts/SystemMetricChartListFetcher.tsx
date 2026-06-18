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
    <div className="grid gap-4 md:grid-cols-1 lg:grid-cols-2 xl:grid-cols-3">
      {data &&
        data.map((chartInfo, i) => (
          <SystemMetricChart
            key={i}
            chartInfo={chartInfo}
            emptyMessage={emptyMessage}
            className="min-h-0 aspect-video"
          />
        ))}
    </div>
  );
};
