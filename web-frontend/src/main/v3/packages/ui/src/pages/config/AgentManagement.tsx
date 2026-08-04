import React from 'react';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { DataTableSkeleton, ErrorBoundary, ScrollArea, Separator } from '../../components';
import { ApplicationCombinedList } from '../../components/Application';
import { AgentManagementFetcher } from '@pinpoint-fe/ui/src/components/Config/agentManagement';

export const AgentManagementPage = () => {
  const [application, setApplication] = React.useState<ApplicationType>();

  return (
    <div className="space-y-6">
      <ScrollArea>
        <div className="flex gap-10">
          <h3 className="text-lg font-semibold">Agent management</h3>
          <ApplicationCombinedList
            open={!application}
            selectedApplication={application}
            onClickApplication={(application) => setApplication(application)}
          />
        </div>
        <Separator className="my-6" />
        {/* agent 목록 조회 실패는 이 영역에서만 fallback을 노출하고, 상단 application 선택 UI는 유지한다. */}
        <ErrorBoundary resetKeys={[application?.applicationName, application?.serviceType]}>
          <React.Suspense fallback={<DataTableSkeleton hideRowBox={true} />}>
            <AgentManagementFetcher
              application={application}
              onRemoveApplicationSuccess={() => setApplication(undefined)}
            />
          </React.Suspense>
        </ErrorBoundary>
      </ScrollArea>
    </div>
  );
};
