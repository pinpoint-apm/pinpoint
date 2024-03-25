import React from 'react';
import { ErrorBoundary } from '../../../Error/ErrorBoundary';
import { DataTableSkeleton } from '../../../DataTable';
import { GroupMemberTableFetcherProps, GroupMemberTableFetcher } from './GroupMemberTableFetcher';

export interface GroupMemberTableProps extends GroupMemberTableFetcherProps {}

export const GroupMemberTable = ({ ...props }: GroupMemberTableProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<DataTableSkeleton hideRowBox={true} />}>
        <GroupMemberTableFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
