import React from 'react';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { DataTableSkeleton, ErrorBoundary, ScrollArea, Separator } from '../../components';
import { ApplicationCombinedList } from '../../components/Application';
import { AgentManagementFetcher } from '@pinpoint-fe/ui/src/components/Config/agentManagement';
import { useIsForbiddenPath } from '@pinpoint-fe/ui/src/hooks';
import { useSetAtom } from 'jotai';
import { resetForbiddenTargetAtom } from '@pinpoint-fe/ui/src/atoms';
import { Forbidden403 } from '../Forbidden403';

export const AgentManagementPage = () => {
  const [application, setApplication] = React.useState<ApplicationType>();
  const isForbidden = useIsForbiddenPath();
  const resetForbiddenTarget = useSetAtom(resetForbiddenTargetAtom);

  return (
    <div className="space-y-6">
      <ScrollArea>
        <div className="flex gap-10">
          <h3 className="text-lg font-semibold">Agent management</h3>
          <ApplicationCombinedList
            open={!application}
            selectedApplication={application}
            onClickApplication={(application) => {
              // 이 화면은 조회 대상을 **경로가 아니라 여기서** 고른다. 대상을 바꿔도 pathname이
              // 그대로라 `useClearForbiddenOnPathChange`가 판정을 버릴 기회가 없다 — 그러면 앞서
              // 받은 403 때문에 다른 application을 골라도 계속 권한 없음 화면에 머문다.
              // 대상이 바뀌는 이 자리에서 버리고, 이전 대상의 요청이 늦게 403을 받아도 새 대상을
              // 덮지 않도록 세대를 올린다. (이슈 #10744)
              resetForbiddenTarget();
              setApplication(application);
            }}
          />
        </div>
        <Separator className="my-6" />
        {/* agent 목록 조회 실패는 이 영역에서만 fallback을 노출하고, 상단 application 선택 UI는 유지한다.
            403도 같다 — 조회 대상을 **경로가 아니라 이 화면 안에서** 고르므로, 선택 박스까지 덮으면
            다른 application으로 바꿀 방법이 사라진다. (이슈 #10744) */}
        {isForbidden ? (
          <Forbidden403 />
        ) : (
          <ErrorBoundary resetKeys={[application?.applicationName, application?.serviceType]}>
            <React.Suspense fallback={<DataTableSkeleton hideRowBox={true} />}>
              <AgentManagementFetcher
                application={application}
                onRemoveApplicationSuccess={() => setApplication(undefined)}
              />
            </React.Suspense>
          </ErrorBoundary>
        )}
      </ScrollArea>
    </div>
  );
};
