import React from 'react';
import { DataTableSkeleton, ErrorBoundary } from '../../components';
import { AgentStatisticFetcher } from '../../components/Config/agentStatistic';
import { useIsForbiddenPath } from '@pinpoint-fe/ui/src/hooks';
import { Forbidden403 } from '../Forbidden403';

export const AgentStatisticPage = () => {
  const isForbidden = useIsForbiddenPath();

  // 관리자 전용 조회라 이 화면에는 고를 대상이 없다. 설정 레이아웃의 헤더와 메뉴는
  // 상위(ConfigurationOutlet)에 있어 그대로 남는다. (이슈 #10744)
  if (isForbidden) {
    return <Forbidden403 />;
  }

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
