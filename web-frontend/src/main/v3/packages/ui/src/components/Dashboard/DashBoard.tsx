import { Responsive, WidthProvider } from 'react-grid-layout';
import { screens } from '@pinpoint-fe/ui/constants';
import ReactGridLayout from 'react-grid-layout';
import {
  DRAGGABLE_CANCEL_CLASS,
  DRAGGABLE_HANDLE_CLASS,
  WIDGET_HEIGHT,
  WIDGET_WIDTH,
} from './Widget';

export interface DashBoardProps extends ReactGridLayout.ResponsiveProps {}

const ResponsiveGridLayout = WidthProvider(Responsive);

export const DASH_BOARD_WIDTH = 24;

export const DashBoard = ({ children, ...props }: DashBoardProps) => {
  const screenSizeMap = Object.keys(screens).reduce(
    (acc, key) => {
      return {
        ...acc,
        [key]: Number(screens[key].replace('px', '')),
      };
    },
    {} as { [key: string]: number },
  );

  return (
    <ResponsiveGridLayout
      isResizable
      breakpoints={{ sm: screenSizeMap['sm'], xxs: 0 }}
      cols={{ sm: DASH_BOARD_WIDTH, xxs: 1 }}
      className="[&>.react-grid-item.react-grid-placeholder]:bg-primary"
      draggableHandle={`.${DRAGGABLE_HANDLE_CLASS}`}
      draggableCancel={`.${DRAGGABLE_CANCEL_CLASS}`}
      {...props}
    >
      {children}
    </ResponsiveGridLayout>
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
