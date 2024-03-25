import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { DataTableSkeleton } from '../../DataTable';
import { UrlSummaryFetcher, UrlSummaryFetcherProps } from './UrlSummaryFetcher';

export interface UrlSummaryProps extends UrlSummaryFetcherProps {}

export const UrlSummary = ({ ...props }: UrlSummaryProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<DataTableSkeleton />}>
        <UrlSummaryFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
