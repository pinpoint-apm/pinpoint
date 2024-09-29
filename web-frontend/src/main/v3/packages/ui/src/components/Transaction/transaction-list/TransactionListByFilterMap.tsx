import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { TransactionListSkeleton } from './TransactionListSkeleton';
import {
  TransactionListByFilterMapFetcher,
  TransactionListByFilterMapFetcherProps,
} from './TransactionListByFilterMapFetcher';

export interface TransactionListProps extends TransactionListByFilterMapFetcherProps {}

export const TransactionListByFilterMap = (props: TransactionListProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<TransactionListSkeleton />}>
        <TransactionListByFilterMapFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
