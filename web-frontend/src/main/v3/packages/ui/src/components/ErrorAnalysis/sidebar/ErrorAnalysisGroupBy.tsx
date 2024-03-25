import React from 'react';
import {
  ErrorAnalysisGroupByFetcher,
  ErrorAnalysisGroupByFetcherProps,
} from './ErrorAnalysisGroupByFetcher';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { ErorrAnalysisGroupBySkeleton } from './ErrorAnalysisGroupBySkeleton';

export interface ErrorAnalysisGroupByProps extends ErrorAnalysisGroupByFetcherProps {}

export const ErrorAnalysisGroupBy = (props: ErrorAnalysisGroupByProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ErorrAnalysisGroupBySkeleton />}>
        <ErrorAnalysisGroupByFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
