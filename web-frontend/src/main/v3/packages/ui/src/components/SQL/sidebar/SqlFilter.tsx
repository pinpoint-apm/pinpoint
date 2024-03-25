import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { SqlFilterFetcher, SqlFilterFetcherProps } from './SqlFilterFetcher';
import { SqlFilterSkeleton } from './SqlFilterSkeleton';

export interface SqlFilterProps extends SqlFilterFetcherProps {}

export const SqlFilter = ({ ...props }: SqlFilterProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<SqlFilterSkeleton />}>
        <SqlFilterFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
