import { cn } from '../../lib';
import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from '@pinpoint-fe/ui/src/components/ui/resizable';
import React from 'react';

export interface LayoutWithContentSidebarProps {
  autoSaveId?: string;
  children: React.ReactNode[];
  contentWrapperClassName?: string;
}

const defaultSizes = [15, 85];

export const LayoutWithContentSidebar = ({
  autoSaveId = '',
  children,
  contentWrapperClassName,
}: LayoutWithContentSidebarProps) => {
  const [sidebar, content, ...rest] = children;

  // 저장된 크기는 그룹의 `autoSaveId` 로 react-resizable-panels 가 직접 복원한다.
  // (3 까지는 저장 형식을 여기서 직접 읽어 defaultSize 로 넣어 줬다.)
  return (
    <>
      <ResizablePanelGroup
        autoSaveId={autoSaveId}
        direction="horizontal"
        className="h-[calc(100%-4rem)]"
      >
        <ResizablePanel id="sidebar" defaultSize={defaultSizes[0]} minSize={10} maxSize={30}>
          {sidebar}
        </ResizablePanel>
        <ResizableHandle withHandle />
        <ResizablePanel id="content" defaultSize={defaultSizes[1]} minSize={70} maxSize={90}>
          <div className="w-full h-[-webkit-fill-available] p-5 pt-4 pb-10 overflow-auto bg-primary-foreground flex justify-center">
            <div className={cn('flex flex-col w-full h-full max-w-8xl', contentWrapperClassName)}>
              {content}
            </div>
          </div>
        </ResizablePanel>
      </ResizablePanelGroup>
      {rest}
    </>
  );
};
