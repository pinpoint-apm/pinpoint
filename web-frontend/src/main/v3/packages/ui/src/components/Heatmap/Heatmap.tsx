import React from 'react';
import { ErrorBoundary, ChartSkeleton } from '@pinpoint-fe/ui';
import { HeatmapFetcher, HeatmapFetcherProps } from './HeatmapFetcher';
import { HeatmapRealtimeFetcher } from './HeatmapRealtimeFetcher';

export interface HeatmapProps extends HeatmapFetcherProps {
  realtime?: boolean;
}

export const Heatmap = ({ realtime = false, ...props }: HeatmapProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ChartSkeleton />}>
        {realtime ? <HeatmapRealtimeFetcher {...props} /> : <HeatmapFetcher {...props} />}
      </React.Suspense>
    </ErrorBoundary>
  );
};
