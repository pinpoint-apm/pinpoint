import React from 'react';
import { Configuration } from '@pinpoint-fe/ui/constants';
import { DataTableSkeleton, ErrorBoundary } from '../../components';
import { AgentManagementFetcher } from '@pinpoint-fe/ui/components/Config/agentManagement';

export interface AgentManagementPageProps {
  configuration?: Configuration;
}

export const AgentManagementPage = (props: AgentManagementPageProps) => {
  return (
    <div className="space-y-6">
      <ErrorBoundary>
        <React.Suspense fallback={<DataTableSkeleton hideRowBox={true} />}>
          <AgentManagementFetcher {...props} />
        </React.Suspense>
      </ErrorBoundary>
    </div>
  );
};
