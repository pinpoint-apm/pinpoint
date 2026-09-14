import {
  LayoutWithSideNavigation as LayoutWithSideNavigationComponent,
  LayoutWithSideNavigationProps,
  SideNavigationMenuItem,
  useServiceSideNavigation,
} from '@pinpoint-fe/ui';
import { FaCog } from 'react-icons/fa';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { LuCircleUser } from 'react-icons/lu';
import { useConfigMenuMap } from './LayoutWithConfiguration';
import { MdOutlineAdminPanelSettings } from 'react-icons/md';
import { useMenuItems } from '@pinpoint-fe/web/src/hooks/useMenuItems';

export const LayoutWithSideNavigation = ({ ...props }: LayoutWithSideNavigationProps) => {
  const CONFIG_MENU_MAP = useConfigMenuMap();
  const { menuItems } = useMenuItems();

  const serviceGroupItems: SideNavigationMenuItem[] = [
    {
      name: `Alarm`,
      // Webhook은 자기 항목이 없다 — Alarm 화면의 탭으로만 오가므로 그 경로에서도 이 항목이
      // 선택된 것으로 보여야 한다.
      path: [APP_PATH.CONFIG_ALARM, APP_PATH.CONFIG_WEBHOOK],
      href: APP_PATH.CONFIG_ALARM,
    },
  ];

  const { serviceMenuItems } = useServiceSideNavigation(serviceGroupItems);

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
      icon: <LuCircleUser />,
      name: 'User',
      path: APP_PATH.CONFIG_GENERAL,
      childItems: CONFIG_MENU_MAP.PERSONAL_SETTINGS.menus,
    },
    ...serviceMenuItems,
  ];

  return (
    <LayoutWithSideNavigationComponent
      topMenuItems={topMenuItems}
      bottomMenuItems={bottomMenuItems}
      {...props}
    />
  );
};
