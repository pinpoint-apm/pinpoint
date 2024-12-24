import React from 'react';
import {
  LayoutWithSideNavigation as LayoutWithSideNavigationComponent,
  LayoutWithSideNavigationProps as LayoutWithSideNavigationComponentProps,
  SideNavigationMenuItem,
} from '@pinpoint-fe/ui';
import { FaCog } from 'react-icons/fa';
import { APP_PATH } from '@pinpoint-fe/ui/constants';
import { LuDoorOpen, LuUserCircle2 } from 'react-icons/lu';
import { CONFIG_MENU_MAP } from './LayoutWithConfiguration';
import { MdOutlineAdminPanelSettings } from 'react-icons/md';
import { useMenuItems } from '@/hooks/useMenuItems';

export interface LayoutWithSideNavigationProps extends LayoutWithSideNavigationComponentProps {}

export const LayoutWithSideNavigation = ({ ...props }: LayoutWithSideNavigationProps) => {
  const { menuItems } = useMenuItems();

  const topMenuItems = menuItems;

  const bottomMenuItems: SideNavigationMenuItem[] = [
    {
      icon: <MdOutlineAdminPanelSettings />,
      name: CONFIG_MENU_MAP.ADMINISTRATION.title,
      path: APP_PATH.CONFIG_USERS,
      childItems: CONFIG_MENU_MAP.ADMINISTRATION.menus,
    },
    {
      icon: <FaCog />,
      name: CONFIG_MENU_MAP.CONFIGURATION.title,
      path: APP_PATH.CONFIG_USER_GROUP,
      childItems: CONFIG_MENU_MAP.CONFIGURATION.menus,
    },
    {
      icon: <LuUserCircle2 />,
      name: 'User',
      path: APP_PATH.CONFIG_GENERAL,
      childItems: CONFIG_MENU_MAP.PERSONAL_SETTINGS.menus,
    },
    {
      children: (collapsed) => (
        <div
          className="flex items-center justify-center gap-2 font-semibold"
          style={{ color: 'aqua' }}
        >
          {collapsed ? '' : 'Go to Pinpoint v2 '}
          <LuDoorOpen />
        </div>
      ),
      name: 'Go to Pinpoint v2',
      path: 'gotoV2',
      aHref: '/v2',
    },
  ];

  return (
    <LayoutWithSideNavigationComponent
      topMenuItems={topMenuItems}
      bottomMenuItems={bottomMenuItems}
      {...props}
    />
  );
};

export const getLayoutWithSideNavigation = (page: React.ReactNode) => {
  return <LayoutWithSideNavigation>{page}</LayoutWithSideNavigation>;
};
