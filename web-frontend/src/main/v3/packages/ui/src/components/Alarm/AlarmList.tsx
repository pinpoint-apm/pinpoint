import React from 'react';
import { ErrorBoundary } from '../../components/Error/ErrorBoundary';
import { AlarmListFetcher, AlarmListFetcherProps } from './AlarmListFetcher';
import { DataTableSkeleton } from '../../components/DataTable/DataTableSkeleton';

export interface AlarmListProps extends AlarmListFetcherProps {}

export const AlarmList = (props: AlarmListProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<DataTableSkeleton hideRowBox />}>
        <AlarmListFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
