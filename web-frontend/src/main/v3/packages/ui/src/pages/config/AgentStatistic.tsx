import React from 'react';
import { Configuration } from '@pinpoint-fe/ui/constants';
import { DataTableSkeleton, ErrorBoundary } from '../../components';
import { AgentStatisticFetcher } from '../../components/Config/agentStatistic';

export interface AgentStatisticPageProps {
  configuration?: Configuration;
}

export const AgentStatisticPage = (props: AgentStatisticPageProps) => {
  return (
    <div className="h-full space-y-6">
      <ErrorBoundary>
        <React.Suspense fallback={<DataTableSkeleton hideRowBox={true} />}>
          <AgentStatisticFetcher {...props} />
        </React.Suspense>
      </ErrorBoundary>
    </div>
  );
};
