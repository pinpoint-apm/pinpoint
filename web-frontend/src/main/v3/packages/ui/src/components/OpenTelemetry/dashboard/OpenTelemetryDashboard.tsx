import React from 'react';
import { ErrorBoundary } from '../..';
import {
  OpenTelemetryDashboardFetcher,
  OpenTelemetryDashboardFetcherProps,
} from './OpenTelemteryDashboardFetcher';
import { OpenTelemetryDashboardSkeleton } from './OpenTelemetryDashboardSkeleton';

export const OpenTelemetryDashboard = ({ ...props }: OpenTelemetryDashboardFetcherProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<OpenTelemetryDashboardSkeleton />}>
        <OpenTelemetryDashboardFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
