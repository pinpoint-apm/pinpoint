import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import {
  SystemMetricChartFetcher,
  SystemMetricChartFetcherProps,
} from './SystemMetricChartFetcher';
import { ChartSkeleton } from '../../Chart';

export interface SystemMetricChartProps extends SystemMetricChartFetcherProps {}

export const SystemMetricChart = ({ ...props }: SystemMetricChartProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense
        fallback={<ChartSkeleton skeletonOption={{ viewBoxWidth: 800, viewBoxHeight: 450 }} />}
      >
        <SystemMetricChartFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
