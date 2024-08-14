import React from 'react';
// import { ServerMapSkeleton } from './ServerMapSkeleton';
import { ErrorBoundary } from '../..';
import {
  MetricDefinitionFormFetcher,
  MetricDefinitionFormFetcherProps,
} from './MeticDefinitionFormFetcher';

export const MetricDefinitionForm = ({ ...props }: MetricDefinitionFormFetcherProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={'loading....'}>
        <MetricDefinitionFormFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
