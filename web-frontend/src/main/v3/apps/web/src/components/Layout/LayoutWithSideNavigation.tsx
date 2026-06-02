import {
  LayoutWithSideNavigation as LayoutWithSideNavigationComponent,
  LayoutWithSideNavigationProps,
  SideNavigationMenuItem,
  useServiceSideNavigation,
} from '@pinpoint-fe/ui';
import { useAtomValue } from 'jotai';
import { FaCog } from 'react-icons/fa';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { LuCircleUser } from 'react-icons/lu';
import { CONFIG_MENU_MAP } from './LayoutWithConfiguration';
import { MdOutlineAdminPanelSettings } from 'react-icons/md';
import { useMenuItems } from '@pinpoint-fe/web/src/hooks/useMenuItems';

export const LayoutWithSideNavigation = ({ ...props }: LayoutWithSideNavigationProps) => {
  const configuration = useAtomValue(configurationAtom);
  const { menuItems } = useMenuItems();

  const serviceGroupItems: SideNavigationMenuItem[] = [
    {
      name: `Alarm`,
      path: APP_PATH.CONFIG_SERVICE_ALARM,
      href: APP_PATH.CONFIG_SERVICE_ALARM,
    },
  ];

  const { serviceMenuItems } = useServiceSideNavigation(configuration, serviceGroupItems);

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
