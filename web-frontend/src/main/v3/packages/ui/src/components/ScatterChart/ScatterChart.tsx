import React from 'react';
import { ScatterChartFetcher, ScatterChartFetcherProps } from './ScatterChartFetcher';
import {
  ScatterChartRealtimeFetcher,
  ScatterChartRealtimeFetcherProps,
} from './ScatterChartRealtimeFetcher';
import { ErrorBoundary, ScatterChartSkeleton } from '../..';

export interface ScatterChartProps
  extends ScatterChartFetcherProps,
    ScatterChartRealtimeFetcherProps {
  realtime?: boolean;
}

export const ScatterChart = ({ realtime = false, ...props }: ScatterChartProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ScatterChartSkeleton />}>
        {realtime ? <ScatterChartRealtimeFetcher {...props} /> : <ScatterChartFetcher {...props} />}
      </React.Suspense>
    </ErrorBoundary>
  );
};
