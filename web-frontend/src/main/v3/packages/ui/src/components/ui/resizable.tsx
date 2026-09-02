import { DragHandleDots2Icon } from '@radix-ui/react-icons';
import React from 'react';
import { Group, Panel, Separator, useDefaultLayout } from 'react-resizable-panels';
import type {
  GroupProps,
  LayoutStorage,
  Orientation,
  PanelProps,
  SeparatorProps,
} from 'react-resizable-panels';

import { cn } from '../../lib';
import { toPanelPercent } from './resizableElements';

// v4 의 Separator 에는 방향을 알려주는 data 속성이 없다(3 까지 있던 `data-panel-group-direction`).
// 핸들 모양이 방향에 따라 갈리므로 그룹이 방향을 내려주고, 핸들이 같은 이름의 속성을 직접 붙인다.
const OrientationContext = React.createContext<Orientation>('horizontal');

// 저장하지 않는 그룹용. `useDefaultLayout` 은 조건부로 부를 수 없으므로 훅은 항상 부르고
// 아무것도 읽고 쓰지 않는 저장소를 준다.
const NO_STORAGE: LayoutStorage = { getItem: () => null, setItem: () => undefined };

export interface ResizablePanelGroupProps extends Omit<
  GroupProps,
  'orientation' | 'defaultLayout' | 'onLayoutChanged'
> {
  direction?: Orientation;
  /**
   * 그룹 레이아웃을 localStorage 에 저장하고 복원할 키. v4 의 `useDefaultLayout` 이
   * `react-resizable-panels:{키}` 에 담으며, 3 까지의 저장 형식도 읽어 주므로 기존에 저장된
   * 크기가 그대로 살아난다. 단 **패널에 고정 `id` 가 있어야** 복원이 맞는 패널로 간다.
   */
  autoSaveId?: string;
}

const ResizablePanelGroup = ({
  className,
  direction = 'horizontal',
  autoSaveId,
  ...props
}: ResizablePanelGroupProps) => {
  const { defaultLayout, onLayoutChanged } = useDefaultLayout({
    id: autoSaveId ?? 'unsaved',
    storage: autoSaveId ? localStorage : NO_STORAGE,
  });

  return (
    <OrientationContext.Provider value={direction}>
      <Group
        className={cn('h-full w-full', className)}
        orientation={direction}
        defaultLayout={defaultLayout}
        onLayoutChanged={onLayoutChanged}
        {...props}
      />
    </OrientationContext.Provider>
  );
};

export interface ResizablePanelProps extends Omit<
  PanelProps,
  'defaultSize' | 'minSize' | 'maxSize'
> {
  /** 부모 그룹에 대한 퍼센트(0~100). */
  defaultSize?: number;
  /** 부모 그룹에 대한 퍼센트(0~100). */
  minSize?: number;
  /** 부모 그룹에 대한 퍼센트(0~100). */
  maxSize?: number;
}

const ResizablePanel = ({ defaultSize, minSize, maxSize, ...props }: ResizablePanelProps) => (
  <Panel
    defaultSize={toPanelPercent(defaultSize)}
    minSize={toPanelPercent(minSize)}
    maxSize={toPanelPercent(maxSize)}
    {...props}
  />
);

const ResizableHandle = ({
  withHandle,
  className,
  ...props
}: SeparatorProps & {
  withHandle?: boolean;
}) => {
  const direction = React.useContext(OrientationContext);

  return (
    <Separator
      data-panel-group-direction={direction}
      className={cn(
        'relative flex w-px items-center justify-center bg-border after:absolute after:inset-y-0 after:left-1/2 after:w-1 after:-translate-x-1/2 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring focus-visible:ring-offset-1 data-[panel-group-direction=vertical]:h-px data-[panel-group-direction=vertical]:w-full data-[panel-group-direction=vertical]:after:left-0 data-[panel-group-direction=vertical]:after:h-1 data-[panel-group-direction=vertical]:after:w-full data-[panel-group-direction=vertical]:after:-translate-y-1/2 data-[panel-group-direction=vertical]:after:translate-x-0 [&[data-panel-group-direction=vertical]>div]:rotate-90',
        className,
      )}
      {...props}
    >
      {withHandle && (
        <div className="z-10 flex h-4 w-3 items-center justify-center rounded-sm border bg-border">
          <DragHandleDots2Icon className="h-2.5 w-2.5" />
        </div>
      )}
    </Separator>
  );
};

export { ResizablePanelGroup, ResizablePanel, ResizableHandle };
