import React from 'react';
import { HeatmapFetcher, HeatmapFetcherProps } from './HeatmapFetcher';
import { ErrorBoundary, ChartSkeleton } from '../..';

export interface HeatmapChartProps extends HeatmapFetcherProps {
  realtime?: boolean;
}

export const HeatmapChart = ({ realtime = false, ...props }: HeatmapChartProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ChartSkeleton />}>
        {realtime ? <div>Realtime heatmap</div> : <HeatmapFetcher {...props} />}
      </React.Suspense>
    </ErrorBoundary>
  );
};
