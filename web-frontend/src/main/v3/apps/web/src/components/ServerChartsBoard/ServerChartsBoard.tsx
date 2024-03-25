import React from 'react';
import { ErrorBoundary } from '@pinpoint-fe/ui';
import {
  ServerChartsBoardFetcher,
  ServerChartsBoardFetcherProps,
} from './ServerChartsBoardFetcher';

export interface ServerChartsBoardProps extends ServerChartsBoardFetcherProps {}

export const ServerChartsBoard = ({ ...props }: ServerChartsBoardProps) => {
  return (
    <ErrorBoundary fallback={<h2>Could not fetch posts.</h2>}>
      <React.Suspense fallback={<h1>Loading posts...</h1>}>
        <ServerChartsBoardFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
