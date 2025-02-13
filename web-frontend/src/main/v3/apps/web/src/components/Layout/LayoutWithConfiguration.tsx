import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import {
  LayoutWithConfiguration as LayoutWithConfigurationComponent,
  LayoutWithConfigurationProps as LayoutWithConfigurationComponentProps,
} from '@pinpoint-fe/ui';
import { useLocation } from 'react-router-dom';

export const CONFIG_MENU_MAP = {
  CONFIGURATION: {
    title: 'Configuration',
    desc: 'Manage Pinpoint settings.',
    menus: [
      {
        name: 'User Group',
        path: APP_PATH.CONFIG_USER_GROUP,
        href: APP_PATH.CONFIG_USER_GROUP,
      },
      {
        name: 'Alarms',
        path: [APP_PATH.CONFIG_ALARM, APP_PATH.CONFIG_WEBHOOK],
        href: APP_PATH.CONFIG_ALARM,
      },
      {
        name: 'Installation',
        path: APP_PATH.CONFIG_INSTALLATION,
        href: APP_PATH.CONFIG_INSTALLATION,
      },
      {
        name: 'Help',
        path: APP_PATH.CONFIG_HELP,
        href: APP_PATH.CONFIG_HELP,
      },
      {
        name: 'Experimental',
        path: APP_PATH.CONFIG_EXPERIMENTAL,
        href: APP_PATH.CONFIG_EXPERIMENTAL,
      },
    ],
  },
  ADMINISTRATION: {
    title: 'Administration',
    desc: 'Manage admin settings.',
    menus: [
      {
        name: 'Users',
        path: APP_PATH.CONFIG_USERS,
        href: APP_PATH.CONFIG_USERS,
      },
      {
        name: 'Agent management',
        path: APP_PATH.CONFIG_AGENT_MANAGEMENT,
        href: APP_PATH.CONFIG_AGENT_MANAGEMENT,
      },
      {
        name: 'Agent statistic',
        path: APP_PATH.CONFIG_AGENT_STATISTIC,
        href: APP_PATH.CONFIG_AGENT_STATISTIC,
      },
    ],
  },
  PERSONAL_SETTINGS: {
    title: 'Personal Settings',
    desc: 'Manage personal settings.',
    menus: [
      {
        name: 'General',
        path: APP_PATH.CONFIG_GENERAL,
        href: APP_PATH.CONFIG_GENERAL,
      },
    ],
  },
};

export interface LayoutWithConfigurationProps extends LayoutWithConfigurationComponentProps {}
export const LayoutWithConfiguration = ({ ...props }: LayoutWithConfigurationProps) => {
  const { pathname } = useLocation();
  const configMenu = Object.values(CONFIG_MENU_MAP).find(({ menus }) => {
    return menus.some(({ path }) => {
      if (typeof path === 'string') {
        return pathname === path;
      } else if (Array.isArray(path)) {
        return path.some((item) => item === pathname);
      }
      return false;
    });
  });

  return <LayoutWithConfigurationComponent configMenu={configMenu} {...props} />;
};

export const getLayoutWithConfiguration = (page: React.ReactNode) => {
  return <LayoutWithConfiguration>{page}</LayoutWithConfiguration>;
};
