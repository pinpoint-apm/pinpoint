import React from 'react';
import { ErrorBoundary } from '../../Error';
import { ChartSkeleton } from '../../Chart';
import { OpenTelemetryMetricFetcherProps } from './OpenTelemetryMetricFetcher';
import { OpenTelemetryMetricFetcher } from './OpenTelemetryMetricFetcher';

export const OpenTelemetryMetric = ({ ...props }: OpenTelemetryMetricFetcherProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense
        fallback={<ChartSkeleton skeletonOption={{ viewBoxWidth: 800, viewBoxHeight: 450 }} />}
      >
        <OpenTelemetryMetricFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
