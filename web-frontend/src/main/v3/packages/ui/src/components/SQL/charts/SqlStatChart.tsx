import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { SqlStatChartFetcher, SqlStatChartFetcherProps } from './SqlStatChartFetcher';
import { SqlStatChartSkeleton } from './SqlStatChartSkeleton';

export interface SqlStatChartProps extends SqlStatChartFetcherProps {}

export const SqlStatChart = ({ ...props }: SqlStatChartProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<SqlStatChartSkeleton />}>
        <SqlStatChartFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
