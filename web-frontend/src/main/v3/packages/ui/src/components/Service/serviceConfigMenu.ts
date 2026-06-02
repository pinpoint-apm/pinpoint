import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import type { ConfigMenu } from '../Layout/LayoutWithConfiguration';

export const SERVICE_CONFIG_MENU: ConfigMenu = {
  title: 'Service',
  desc: 'Manage services.',
  menus: [
    {
      name: 'Alarm',
      path: APP_PATH.CONFIG_SERVICE_ALARM,
      href: APP_PATH.CONFIG_SERVICE_ALARM,
    },
  ],
};

export const isServiceConfigPath = (pathname: string) =>
  pathname === APP_PATH.CONFIG_SERVICE_SETTING || pathname === APP_PATH.CONFIG_SERVICE_ALARM;
