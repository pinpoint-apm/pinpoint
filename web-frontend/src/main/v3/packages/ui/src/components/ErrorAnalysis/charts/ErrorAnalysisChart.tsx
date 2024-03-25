import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import {
  ErrorAnalysisChartFetcher,
  ErrorAnalysisChartFetcherProps,
} from './ErrorAnalysisChartFetcher';
import { ErrorAnalysisChartSkeleton } from './ErrorAnalysisSkeleton';

export interface ErrorAnalysisChartProps extends ErrorAnalysisChartFetcherProps {}

export const ErrorAnalysisChart = ({ ...props }: ErrorAnalysisChartProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ErrorAnalysisChartSkeleton />}>
        <ErrorAnalysisChartFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
