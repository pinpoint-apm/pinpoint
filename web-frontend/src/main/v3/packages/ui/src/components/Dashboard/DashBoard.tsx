import { Responsive, useContainerWidth } from 'react-grid-layout';
import type { ResponsiveProps } from 'react-grid-layout';
import { screenPixels } from '@pinpoint-fe/ui/src/constants';
import {
  DRAGGABLE_CANCEL_CLASS,
  DRAGGABLE_HANDLE_CLASS,
  WIDGET_HEIGHT,
  WIDGET_WIDTH,
} from './Widget';

// `width` 는 컨테이너를 재는 쪽에서 넣어주므로 바깥에서 받지 않는다. react-grid-layout 1 에서는
// `WidthProvider` HOC 가 그 일을 했는데, 2 에서 없어지고 `useContainerWidth` 훅으로 바뀌었다.
export interface DashBoardProps extends Omit<ResponsiveProps, 'width'> {}

export const DASH_BOARD_WIDTH = 24;

export const DashBoard = ({ children, ...props }: DashBoardProps) => {
  const { width, containerRef } = useContainerWidth();
  return (
    <div ref={containerRef}>
      <Responsive
        width={width}
        breakpoints={{ sm: screenPixels.sm, xxs: 0 }}
        cols={{ sm: DASH_BOARD_WIDTH, xxs: 1 }}
        className="[&>.react-grid-item.react-grid-placeholder]:bg-primary"
        // react-grid-layout 2 는 drag/resize 설정을 개별 prop 이 아니라 설정 객체로 받는다.
        // (`isResizable` → `resizeConfig.enabled`, `draggableHandle` → `dragConfig.handle`)
        resizeConfig={{ enabled: true }}
        dragConfig={{
          handle: `.${DRAGGABLE_HANDLE_CLASS}`,
          cancel: `.${DRAGGABLE_CANCEL_CLASS}`,
        }}
        {...props}
      >
        {children}
      </Responsive>
    </div>
  );
};

export const getNewWidgetLayout = (
  metrics: { layout: { x: number; y: number; h: number; w: number } }[],
  props?: {
    newWidgetWidth?: number;
    newWidgetHeight?: number;
    dashBoardWidth?: number;
  },
) => {
  if (metrics?.length) {
    const { newWidgetWidth, newWidgetHeight, dashBoardWidth } = {
      newWidgetWidth: WIDGET_WIDTH,
      newWidgetHeight: WIDGET_HEIGHT,
      dashBoardWidth: DASH_BOARD_WIDTH,
      ...props,
    };

    for (let y = 0; y < Infinity; y++) {
      for (let x = 0; x <= dashBoardWidth - newWidgetWidth; x++) {
        let isSpaceAvailable = true;

        for (let i = 0; i < metrics.length; i++) {
          const itemLayout = metrics[i].layout;
          if (
            x < itemLayout.x + itemLayout.w &&
            x + newWidgetWidth > itemLayout.x &&
            y < itemLayout.y + itemLayout.h &&
            y + newWidgetHeight > itemLayout.y
          ) {
            isSpaceAvailable = false;
            break;
          }
        }

        if (isSpaceAvailable) {
          return { x, y, w: WIDGET_WIDTH, h: WIDGET_HEIGHT };
        }
      }
    }
  }

  return { x: 0, y: 0, w: WIDGET_WIDTH, h: WIDGET_HEIGHT };
};
