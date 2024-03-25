import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { SqlGroupByOptionFetcher, SqlGroupByOptionFetcherProps } from './SqlGroupByOptionFetcher';
import { SqlGroupByOptionSkeleton } from './SqlGroupByOptionSkeleton';

export interface SqlGroupByOptionProps extends SqlGroupByOptionFetcherProps {}

export const SqlGroupByOption = ({ ...props }: SqlGroupByOptionProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<SqlGroupByOptionSkeleton />}>
        <SqlGroupByOptionFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
