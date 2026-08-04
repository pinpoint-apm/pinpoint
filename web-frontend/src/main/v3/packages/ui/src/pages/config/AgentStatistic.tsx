import React from 'react';
import { DataTableSkeleton, ErrorBoundary } from '../../components';
import { AgentStatisticFetcher } from '../../components/Config/agentStatistic';

export const AgentStatisticPage = () => {
  return (
    <div className="h-full space-y-6">
      <ErrorBoundary>
        <React.Suspense fallback={<DataTableSkeleton hideRowBox={true} />}>
          <AgentStatisticFetcher />
        </React.Suspense>
      </ErrorBoundary>
    </div>
  );
};
