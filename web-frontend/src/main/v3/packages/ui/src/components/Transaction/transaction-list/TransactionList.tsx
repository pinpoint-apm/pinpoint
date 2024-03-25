import React from 'react';
import { TransactionListFetcher, TransactionListFetcherProps } from './TransactionListFetcher';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { TransactionListSkeleton } from './TransactionListSkeleton';

export interface TransactionListProps extends TransactionListFetcherProps {}

export const TransactionList = (props: TransactionListProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<TransactionListSkeleton />}>
        <TransactionListFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
