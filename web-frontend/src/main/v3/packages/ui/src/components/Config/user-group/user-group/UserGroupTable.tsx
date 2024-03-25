import React from 'react';
import { ErrorBoundary } from '../../../Error/ErrorBoundary';
import { UserGroupTableFetcherProps, UserGroupTableFetcher } from './UserGroupTableFetcher';
import { DataTableSkeleton } from '../../../DataTable';

export interface UserGroupTableProps extends UserGroupTableFetcherProps {}

export const UserGroupTable = ({ ...props }: UserGroupTableProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<DataTableSkeleton hideRowBox={true} />}>
        <UserGroupTableFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
