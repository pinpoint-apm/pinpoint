import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import {
  SystemMetricChartListFetcher,
  SystemMetricChartListFetcherProps,
} from './SystemMetricChartListFetcher';
import { SystemMetricChartListSkeleton } from './SystemMetricChartListSkeleton';

export interface SystemMetricChartListProps extends SystemMetricChartListFetcherProps {}

export const SystemMetricChartList = ({ ...props }: SystemMetricChartListProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<SystemMetricChartListSkeleton />}>
        <SystemMetricChartListFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
