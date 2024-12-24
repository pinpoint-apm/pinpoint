import React from 'react';
import {
  getPanelElement,
  getPanelGroupElement,
  getResizeHandleElement,
} from 'react-resizable-panels';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/constants';
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '..';
import { useLocalStorage } from '@pinpoint-fe/ui/hooks';
import { cn } from '../../lib';

interface SizeInfo {
  minSize: number;
  maxSize: number;
  currentPanelWidth: number;
  resizeHandleWidth: number;
  SERVER_LIST_WIDTH: number;
}

interface ResizableRefsType {
  groupElement: HTMLElement | null;
  rightPanelElement: HTMLElement | null;
  resizeHandleElement: HTMLElement | null;
}

export interface LayoutWithHorizontalResizableProps {
  children: (React.ReactNode | ((size: SizeInfo) => React.ReactNode))[];
  withHandle?: boolean;
  disabled?: boolean;
}

const resizableId = APP_SETTING_KEYS.SERVER_MAP_HORIZONTAL_RESIZABLE;

export const LayoutWithHorizontalResizable = ({
  children,
  withHandle = true,
  disabled = false,
}: LayoutWithHorizontalResizableProps) => {
  const [leftPanelContent, rightPanelContent] = children;
  const sizes = useLayoutWithHorizontalResizable();

  return (
    <ResizablePanelGroup direction="horizontal" id={resizableId} autoSaveId={resizableId}>
      <ResizablePanel>
        {typeof leftPanelContent === 'function' ? leftPanelContent(sizes) : leftPanelContent}
      </ResizablePanel>
      <ResizableHandle
        id={resizableId}
        className={cn('!w-1.5 z-[1100]', {
          '!pointer-events-none': disabled,
        })}
        withHandle={withHandle}
        disabled={disabled}
      />
      <ResizablePanel
        id={resizableId}
        className="z-[1099] min-w-[500px]"
        minSize={sizes.minSize}
        maxSize={sizes.maxSize}
      >
        {typeof rightPanelContent === 'function' ? rightPanelContent(sizes) : rightPanelContent}
      </ResizablePanel>
    </ResizablePanelGroup>
  );
};

export const useLayoutWithHorizontalResizable = () => {
  const SERVER_LIST_WIDTH = 300;
  const MIN_SIZE_IN_PIXEL = 500;
  const [defaultSize, setDefaultSize] = useLocalStorage(resizableId, {
    minSize: 30,
    maxSize: 70,
    resizeHandleWidth: 6,
    currentPanelWidth: 500,
  });
  const [sizes, setSizes] = React.useState<SizeInfo>({
    minSize: defaultSize.minSize,
    maxSize: defaultSize.maxSize,
    currentPanelWidth: defaultSize.currentPanelWidth,
    resizeHandleWidth: defaultSize.resizeHandleWidth,
    SERVER_LIST_WIDTH,
  });
  const resizableRef = React.useRef<ResizableRefsType>();

  React.useEffect(() => {
    const groupElement = getPanelGroupElement(resizableId);
    const rightPanelElement = getPanelElement(resizableId);
    const resizeHandleElement = getResizeHandleElement(resizableId);

    resizableRef.current = {
      groupElement,
      rightPanelElement,
      resizeHandleElement,
    };
  }, []);

  React.useEffect(() => {
    if (!resizableRef.current) return;
    const { groupElement, rightPanelElement, resizeHandleElement } = resizableRef.current;
    let observer: ResizeObserver;

    if (groupElement && resizeHandleElement && rightPanelElement) {
      observer = new ResizeObserver(() => {
        const containerLength = groupElement.clientWidth;
        if (containerLength > 0) {
          const resizeHandleWidth = resizeHandleElement.clientWidth;

          const minSize = (MIN_SIZE_IN_PIXEL / (containerLength - resizeHandleWidth)) * 100;
          const calculatedMaxSize =
            ((containerLength - resizeHandleWidth - SERVER_LIST_WIDTH) / 2 / containerLength) * 100;
          const maxSize = calculatedMaxSize > minSize ? calculatedMaxSize : minSize;
          const currentPanelWidth = rightPanelElement.clientWidth;

          if (minSize + maxSize > 100) {
            setDefaultSize({ minSize: 30, maxSize: 70, currentPanelWidth, resizeHandleWidth });

            setSizes((prev) => ({
              ...prev,
              maxSize: 30,
              minSize: 70,
              currentPanelWidth,
              resizeHandleWidth,
            }));
          } else {
            setDefaultSize({ minSize, maxSize, currentPanelWidth, resizeHandleWidth });

            setSizes((prev) => ({
              ...prev,
              maxSize,
              minSize,
              currentPanelWidth,
              resizeHandleWidth,
            }));
          }
        }
      });
      observer.observe(rightPanelElement);
    }

    return () => {
      observer?.disconnect();
    };
  }, [resizableRef]);

  return sizes;
};
