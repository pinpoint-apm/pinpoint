import 'rc-drawer/assets/index.css';

import RcDrawer, { DrawerProps as RcDrawerProps } from 'rc-drawer';
import motionProps from './motion';
import { zIndex } from '../../constants/theme';

export interface DrawerProps extends RcDrawerProps {}

export const Drawer = (props: DrawerProps) => {
  return (
    <RcDrawer
      // width='20vw'
      placement="right"
      maskClosable={true}
      rootStyle={{
        position: 'static',
      }}
      maskStyle={{
        zIndex: 1,
      }}
      className="[&.rc-drawer-content]:flex [&.rc-drawer-content]:overflow-hidden"
      {...motionProps}
      {...props}
      styles={{
        ...props.styles,
        // 라이브러리 CSS 의 기본값(1050)을 덮는다. 그 CSS 는 cascade layer 밖이라 유틸리티 클래스로는
        // 이길 수 없어 인라인으로 싣는다. 호출부가 wrapper 에 z-index 를 주면 그 값이 이긴다.
        wrapper: { zIndex: zIndex.drawer, ...props.styles?.wrapper },
      }}
      // onClose={() => setOpen(false)}
      // {...motionProps}
    >
      {props?.children}
    </RcDrawer>
  );
};
