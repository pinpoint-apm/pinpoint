import React from 'react';
import { DataTableSkeleton } from '../../DataTable';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import {
  ErrorAnalysisGroupedTableFetcher,
  ErrorAnalysisGroupedTableFetcherProps,
} from './ErrorAnalysisGroupedTableFetcher';

export interface ErrorAnalysisGroupedTableProps extends ErrorAnalysisGroupedTableFetcherProps {}

export const ErrorAnalysisGroupedTable = (props: ErrorAnalysisGroupedTableProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<DataTableSkeleton hideRowBox />}>
        <ErrorAnalysisGroupedTableFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
