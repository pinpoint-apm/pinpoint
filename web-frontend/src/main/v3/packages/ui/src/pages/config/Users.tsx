import { DataTableSkeleton, ErrorBoundary } from '../../components';
import { UsersTableFetcher } from '../../components/Config/users/UsersTableFetcher';
import React from 'react';

export const UsersPage = () => {
  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Users</h3>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-px w-full"
      ></div>
      <ErrorBoundary>
        <React.Suspense fallback={<DataTableSkeleton hideRowBox={true} />}>
          <UsersTableFetcher />
        </React.Suspense>
      </ErrorBoundary>
    </div>
  );
};
