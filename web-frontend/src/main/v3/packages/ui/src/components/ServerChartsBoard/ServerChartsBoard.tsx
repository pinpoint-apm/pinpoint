import React from 'react';
import { ChartBoardSkeleton, ErrorBoundary } from '../';
import {
  ServerChartsBoardFetcher,
  ServerChartsBoardFetcherProps,
} from './ServerChartsBoardFetcher';

export interface ServerChartsBoardProps extends ServerChartsBoardFetcherProps {}

export const ServerChartsBoard = ({ ...props }: ServerChartsBoardProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ChartBoardSkeleton />}>
        <ServerChartsBoardFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
