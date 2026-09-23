import React from 'react';
import { SiDatabricks } from 'react-icons/si';
import { useIsForbiddenPath, useSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { Forbidden403 } from './Forbidden403';
import { MainHeader } from '../components/MainHeader';
import { ApplicationCombinedList } from '../components/Application';
import { ThreadDumpList } from '../components/ThreadDump/ThreadDumpList';
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '../components/ui/resizable';
import { ThreadDumpDetail } from '../components/ThreadDump/ThreadDumpDetail';
import { ActiveThreadLightDump } from '@pinpoint-fe/ui/src/constants';

export const ThreadDumpPage = () => {
  const [selectedThread, setSelectedThread] =
    React.useState<ActiveThreadLightDump.ThreadDumpData>();
  const { application, searchParameters } = useSearchParameters();
  const agentId = searchParameters.agentId;
  const isForbidden = useIsForbiddenPath();

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <SiDatabricks /> Thread Dump
          </div>
        }
      >
        <ApplicationCombinedList selectedApplication={application} disabled />
        {application && (
          <div className="flex items-center gap-1 ml-4 font-semibold truncate">
            <div className="truncate">({agentId})</div>
          </div>
        )}
      </MainHeader>
      {/* 이 화면의 API 중 하나가 403을 받았다. **헤더는 남기고 본문만 바꾼다** — 본문을 언마운트해
          남은 조회들도 함께 멈춘다. 이 화면의 선택 박스는 표시 전용(disabled)이라 대상은 경로로만
          바뀌고, 경로가 바뀌면 판정도 함께 버려진다(`useClearForbiddenOnPathChange`).
          (어느 화면이 이 판정에서 빠지는지는 `coversPageOnForbidden`. 이슈 #10744) */}
      {isForbidden && <Forbidden403 />}
      {!isForbidden && (
        <ResizablePanelGroup
          direction="vertical"
          // autoSaveId={APP_SETTING_KEYS.TRANSACTION_LIST_RESIZABLE}
        >
          <ResizablePanel minSize={10} maxSize={90}>
            <ThreadDumpList
              selectedThread={selectedThread}
              onClickRow={(rowData) => setSelectedThread(rowData.original)}
            />
          </ResizablePanel>
          <ResizableHandle withHandle />
          <ResizablePanel minSize={10} maxSize={90}>
            <ThreadDumpDetail thread={selectedThread} />
          </ResizablePanel>
        </ResizablePanelGroup>
      )}
    </div>
  );
};
