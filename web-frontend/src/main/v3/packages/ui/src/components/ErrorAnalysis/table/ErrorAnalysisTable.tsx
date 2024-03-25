import React from 'react';
import {
  ErrorAnalysisTableFetcher,
  ErrorAnalysisTableFetcherProps,
} from './ErrorAnalysisTableFetcher';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { DataTableSkeleton } from '../../DataTable';

export interface ErrorAnalysisTableProps extends ErrorAnalysisTableFetcherProps {}

export const ErrorAnalysisTable = (props: ErrorAnalysisTableProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<DataTableSkeleton />}>
        <ErrorAnalysisTableFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
