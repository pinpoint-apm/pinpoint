import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { SqlSummaryFetcherProps, SqlSummaryFetcher } from './SqlSummaryFetcher';
import { DataTableSkeleton } from '../../DataTable/DataTableSkeleton';

export interface SqlSummaryProps extends SqlSummaryFetcherProps {}

export const SqlSummary = (props: SqlSummaryProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<DataTableSkeleton />}>
        <SqlSummaryFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
