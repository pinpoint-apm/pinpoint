import { cn } from '../../lib';
import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from '@pinpoint-fe/ui/src/components/ui/resizable';

export interface LayoutWithContentSidebarProps {
  children: React.ReactNode[];
  contentWrapperClassName?: string;
}

export const LayoutWithContentSidebar = ({
  children,
  contentWrapperClassName,
}: LayoutWithContentSidebarProps) => {
  const [sidebar, content, ...rest] = children;

  return (
    <>
      <ResizablePanelGroup direction="horizontal" className="h-[calc(100%-4rem)]">
        <ResizablePanel defaultSize={15} minSize={10} maxSize={30}>
          {sidebar}
        </ResizablePanel>
        <ResizableHandle withHandle />
        <ResizablePanel>
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
