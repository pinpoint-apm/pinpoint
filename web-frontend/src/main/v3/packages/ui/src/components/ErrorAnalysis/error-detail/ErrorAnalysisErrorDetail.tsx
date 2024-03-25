import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import {
  ErrorAnalysisErrorDetailFetcherProps,
  ErrorAnalysisErrorDetailFetcher,
} from './ErrorAnalysisErrorDetailFetcher';
import { ErrorAnalysisErrorDetailSkeleton } from './ErrorAnalysisErrorDetailSkeleton';

export interface ErrorAnalysisErrorDetailProps extends ErrorAnalysisErrorDetailFetcherProps {}

export const ErrorAnalysisErrorDetail = ({ ...props }: ErrorAnalysisErrorDetailProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ErrorAnalysisErrorDetailSkeleton />}>
        <ErrorAnalysisErrorDetailFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
