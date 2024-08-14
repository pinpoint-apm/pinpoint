import { Responsive, WidthProvider } from 'react-grid-layout';
import { screens } from '../../constant';
import ReactGridLayout from 'react-grid-layout';
import { DRAGGABLE_CANCEL_CLASS, DRAGGABLE_HANDLE_CLASS } from './Widget';

export interface DashBoardProps extends ReactGridLayout.ResponsiveProps {}

const ResponsiveGridLayout = WidthProvider(Responsive);

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
      cols={{ sm: 12, xxs: 1 }}
      className="[&>.react-grid-item.react-grid-placeholder]:bg-primary"
      draggableHandle={`.${DRAGGABLE_HANDLE_CLASS}`}
      draggableCancel={`.${DRAGGABLE_CANCEL_CLASS}`}
      {...props}
    >
      {children}
    </ResponsiveGridLayout>
  );
};
