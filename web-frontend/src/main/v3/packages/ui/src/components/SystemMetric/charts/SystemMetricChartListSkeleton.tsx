import { ChartSkeleton } from '../../Chart';

export interface SystemMetricChartListSkeletonProps {}

export const SystemMetricChartListSkeleton = () => {
  return (
    <div className="grid md:grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
      {Array(3)
        .fill(0)
        .map((_, i) => (
          <ChartSkeleton key={i} skeletonOption={{ viewBoxWidth: 800, viewBoxHeight: 450 }} />
        ))}
    </div>
  );
};
