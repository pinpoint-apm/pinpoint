import React from 'react';
import { TransactionInfoFetcher, TransactionInfoFetcherProps } from '.';
import { ErrorBoundary } from '../../Error';
import { TransactionInfoSkeleton } from './TransactionInfoSkeleton';

export interface TransactionInfoProps extends TransactionInfoFetcherProps {}

export const TransactionInfo = (props: TransactionInfoProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<TransactionInfoSkeleton />}>
        <TransactionInfoFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
