import React from 'react';
// import { ServerMapSkeleton } from './ServerMapSkeleton';
import { ErrorBoundary } from '../..';
import {
  MetricDefinitionFormFetcher,
  MetricDefinitionFormFetcherProps,
} from './MetricDefinitionFormFetcher';
import { ListItemSkeleton } from '../../VirtualList';

export const MetricDefinitionForm = ({ ...props }: MetricDefinitionFormFetcherProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ListItemSkeleton skeletonOption={{ itemHeight: 10 }} />}>
        <MetricDefinitionFormFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
