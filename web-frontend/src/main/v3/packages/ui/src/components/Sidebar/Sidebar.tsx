import React from 'react';
import {
  Menu,
  MenuItem,
  Sidebar as ProSidebar,
  SubMenu,
  sidebarClasses,
  MenuItemStyles,
  ElementStyles,
  MenuItemStylesParams,
} from 'react-pro-sidebar';
import { APP_SETTING_KEYS } from '@pinpoint-fe/constants';
import { cn } from '../../lib';
import { useLocalStorage } from '@pinpoint-fe/ui/hooks';
import { LuChevronFirst, LuChevronLast } from 'react-icons/lu';

export interface SideNavigationProps {
  header?: React.ReactNode | ((collapse: boolean) => React.ReactNode);
  children?: React.ReactNode;
}

const SIDEBAR_WIDTH = 200;
const SIDEBAR_COLLAPSED_WIDTH = 64;

const Sidebar = ({ children, header }: SideNavigationProps) => {
  const [collapsed, setCollapsed] = useLocalStorage(APP_SETTING_KEYS.SIDE_NAV_BAR_SCALE, false);

  React.useEffect(() => {
    setCollapsed(collapsed);
  }, [collapsed]);

  return (
    <ProSidebar
      className="[&_.scale-button-wrapper]:hover:inline-block"
      width={`${SIDEBAR_WIDTH}px`}
      collapsed={collapsed}
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
        className={cn(
          'relative h-16 min-h-[4rem] flex items-center pl-6 hover:bg-[--blue-700] mb-2',
          {
            'justify-center pl-0 text-center': collapsed,
          },
        )}
      >
        {typeof header === 'function' ? header(collapsed) : header}
        <div className="absolute hidden scale-button-wrapper top-1 right-1">
          <button
            className="flex items-center justify-center w-6 h-6 opacity-50 cursor-pointer hover:opacity-100 hover:font-semibold "
            onClick={() => setCollapsed(!collapsed)}
          >
            {collapsed ? <LuChevronLast /> : <LuChevronFirst />}
          </button>
        </div>
      </div>
      <React.Fragment>{children}</React.Fragment>
    </ProSidebar>
  );
};

const getMenuItemStyle = ({
  level,
  active,
  isSubmenu,
  open,
}: MenuItemStylesParams): ElementStyles => {
  const isSubmenuGroup = isSubmenu && level === 0;
  let backgroundColor = '';

  if (isSubmenuGroup && open) {
    backgroundColor = 'var(--snb-submenu-background)';
  }
  if ((active && !isSubmenuGroup) || (active && isSubmenuGroup && !open)) {
    backgroundColor = 'var(--blue-700)';
  }

  return {
    padding: '0px 0.5rem 0px 0.375rem',
    paddingLeft: level === 1 ? '1.5rem' : '',
    height: '2.5rem',
    borderRadius: '0.25rem',
    borderBottomLeftRadius: isSubmenuGroup && open ? 0 : '',
    borderBottomRightRadius: isSubmenuGroup && open ? 0 : '',
    fontWeight: active ? 600 : 'unset',
    backgroundColor,
    fontSize: '0.875rem',
    '&:hover': {
      color: 'var(--white-default)',
      backgroundColor: 'var(--blue-700)',
      opacity: 1,
    },
  };
};

const opacityHelper = (active: boolean) => {
  return {
    opacity: active ? 1 : 0.7,
  };
};

const menuItemStyles: MenuItemStyles = {
  icon: ({ active }) => ({
    marginRight: '0.25rem',
    ...opacityHelper(active),
  }),
  label: ({ active }) => ({
    ...opacityHelper(active),
  }),
  button: (props) => {
    return {
      ...getMenuItemStyle(props),
    };
  },
};

const bottomMenuItemStyles: MenuItemStyles = {
  icon: ({ active }) => ({
    marginRight: '0.25rem',
    ...opacityHelper(active),
  }),
  label: ({ active }) => ({
    ...opacityHelper(active),
  }),
  button: (props) => {
    return {
      ...getMenuItemStyle(props),
    };
  },
  subMenuContent: ({ open }) => {
    return {
      borderRadius: '0.25rem',
      borderTopLeftRadius: open ? 0 : '',
      borderTopRightRadius: open ? 0 : '',
      backgroundColor: 'var(--snb-submenu-background)',
    };
  },
  SubMenuExpandIcon: () => ({
    height: `100%`,
    display: `flex`,
    alignItems: 'center',
  }),
};

export {
  Menu,
  MenuItem,
  Sidebar,
  SubMenu,
  menuItemStyles,
  bottomMenuItemStyles,
  SIDEBAR_COLLAPSED_WIDTH,
  SIDEBAR_WIDTH,
};
