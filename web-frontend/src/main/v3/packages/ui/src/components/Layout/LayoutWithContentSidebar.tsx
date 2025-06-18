import { cn } from '../../lib';
import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from '@pinpoint-fe/ui/src/components/ui/resizable';
import { layoutWithContentSidebarAtom } from '@pinpoint-fe/ui/src/atoms/layoutWithContentSidebar';
import { useAtom } from 'jotai';

export interface LayoutWithContentSidebarProps {
  children: React.ReactNode[];
  contentWrapperClassName?: string;
}

export const LayoutWithContentSidebar = ({
  children,
  contentWrapperClassName,
}: LayoutWithContentSidebarProps) => {
  const [sidebar, content, ...rest] = children;
  const [sizes, setSizes] = useAtom(layoutWithContentSidebarAtom);

  function handleLayout(sizes: number[]) {
    setSizes(sizes);
  }

  return (
    <>
      <ResizablePanelGroup
        direction="horizontal"
        className="h-[calc(100%-4rem)]"
        onLayout={handleLayout}
      >
        <ResizablePanel defaultSize={sizes?.[0] || 15} minSize={10} maxSize={30}>
          {sidebar}
        </ResizablePanel>
        <ResizableHandle withHandle />
        <ResizablePanel defaultSize={sizes?.[1] || 85} minSize={70} maxSize={90}>
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
