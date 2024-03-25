import 'rc-drawer/assets/index.css';

import RcDrawer, { DrawerProps as RcDrawerProps } from 'rc-drawer';
import motionProps from './motion';

export interface DrawerProps extends RcDrawerProps {}

export const Drawer = (props: DrawerProps) => {
  return (
    <RcDrawer
      // width='20vw'
      placement="right"
      maskClosable={true}
      rootStyle={{
        zIndex: 1,
        position: 'static',
      }}
      maskStyle={{
        zIndex: 1,
      }}
      className="[&.rc-drawer-content]:flex [&.rc-drawer-content]:overflow-hidden"
      {...motionProps}
      {...props}
      // onClose={() => setOpen(false)}
      // {...motionProps}
    >
      {props?.children}
    </RcDrawer>
  );
};
