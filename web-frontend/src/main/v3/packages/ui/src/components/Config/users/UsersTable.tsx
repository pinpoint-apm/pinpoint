import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { DataTableSkeleton } from '../../DataTable';
import { UsersTableAction, UsersTableFetcher, UsersTableFetcherProps } from './UsersTableFetcher';
export type { UsersTableAction } from './UsersTableFetcher';

export interface UsersTableProps extends UsersTableFetcherProps {}

export const UsersTable = React.forwardRef<UsersTableAction, UsersTableProps>(
  ({ ...props }: UsersTableProps, ref) => {
    return (
      <ErrorBoundary>
        <React.Suspense fallback={<DataTableSkeleton hideRowBox={true} />}>
          <UsersTableFetcher {...props} ref={ref} />
        </React.Suspense>
      </ErrorBoundary>
    );
  },
);
