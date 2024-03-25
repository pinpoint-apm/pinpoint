import React from 'react';
import {
  useProSidebar,
  Menu,
  MenuItem,
  Sidebar as ProSidebar,
  ProSidebarProvider as SidebarProvider,
  SubMenu,
  sidebarClasses,
  MenuItemStyles,
} from 'react-pro-sidebar';
import { useUpdateEffect } from 'usehooks-ts';
import { APP_SETTING_KEYS } from '@pinpoint-fe/constants';
import { cn } from '../../lib';
import { useLocalStorage } from '@pinpoint-fe/hooks';
import { LuChevronFirst, LuChevronLast } from 'react-icons/lu';

export interface SideNavigationProps {
  header?: React.ReactNode | ((collapse: boolean) => React.ReactNode);
  children?: React.ReactNode;
}

const SIDEBAR_WIDTH = 200;
const SIDEBAR_COLLAPSED_WIDTH = 80;

const Sidebar = ({ children, header }: SideNavigationProps) => {
  const { collapseSidebar, collapsed } = useProSidebar();
  const [sidebarScale, setSidebarScale] = useLocalStorage(
    APP_SETTING_KEYS.SIDE_NAV_BAR_SCALE,
    false,
  );

  useUpdateEffect(() => {
    setSidebarScale(collapsed);
  }, [collapsed]);

  return (
    <ProSidebar
      className="[&_.scale-button-wrapper]:hover:inline-block"
      defaultCollapsed={sidebarScale}
      width={`${SIDEBAR_WIDTH}px`}
      collapsedWidth={`${SIDEBAR_COLLAPSED_WIDTH}px`}
      rootStyles={{
        color: 'var(--snb-text)',
        [`.${sidebarClasses.container}`]: {
          display: 'flex',
          flexDirection: 'column',
          background: 'var(--snb-background)',
          maxWidth: SIDEBAR_WIDTH,
          paddingBottom: 40,
        },
      }}
    >
      <div
        className={cn('relative h-32 min-h-[8rem] flex items-center pl-5 hover:bg-[--blue-700]', {
          'justify-center pl-0 text-center': collapsed,
        })}
      >
        {typeof header === 'function' ? header(collapsed) : header}
        <button
          className="scale-button-wrapper hidden opacity-50 hover:opacity-100 items-center justify-center absolute top-2 right-2 w-6 h-6 cursor-pointer rounded hover:font-semibold hover:bg-[var(--blue-900)]"
          onClick={() => collapseSidebar()}
        >
          {collapsed ? <LuChevronLast /> : <LuChevronFirst />}
        </button>
      </div>
      <React.Fragment>{children}</React.Fragment>
    </ProSidebar>
  );
};

const menuItemStyles: MenuItemStyles = {
  icon: {
    fontSize: '1rem',
    marginRight: '4px',
  },
  button: ({ active }) => ({
    height: 45,
    fontWeight: active ? 600 : 'unset',
    color: 'var(--white-default)',
    opacity: active ? 1 : 0.65,
    '&:hover': {
      color: 'var(--white-default)',
      backgroundColor: 'var(--blue-800)',
      opacity: 1,
    },
  }),
};

const bottomMenuItemStyles: MenuItemStyles = {
  icon: {
    marginRight: '4px',
  },
  button: ({ active }) => ({
    height: 45,
    fontWeight: active ? 600 : 'unset',
    color: 'var(--white-default)',
    opacity: active ? 1 : 0.65,
    '&:hover': {
      color: 'var(--white-default)',
      backgroundColor: 'var(--blue-700)',
      opacity: 1,
    },
  }),
  subMenuContent: () => ({
    backgroundColor: 'var(--snb-submenu-background)',
  }),
  SubMenuExpandIcon: () => ({
    height: `100%`,
    display: `flex`,
    alignItems: 'center',
  }),
};

export {
  Menu,
  MenuItem,
  SidebarProvider,
  Sidebar,
  SubMenu,
  menuItemStyles,
  bottomMenuItemStyles,
  useProSidebar,
  SIDEBAR_COLLAPSED_WIDTH,
  SIDEBAR_WIDTH,
};
