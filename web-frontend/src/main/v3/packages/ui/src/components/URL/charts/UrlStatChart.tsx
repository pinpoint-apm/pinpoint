import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { UrlStatChartFetcher, UrlStatChartFetcherProps } from './UrlStatChartFetcher';
import { ChartSkeleton } from '../../Chart';

export interface UrlStatChartProps extends UrlStatChartFetcherProps {}

export const UrlStatChart = ({ ...props }: UrlStatChartProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ChartSkeleton />}>
        <UrlStatChartFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
