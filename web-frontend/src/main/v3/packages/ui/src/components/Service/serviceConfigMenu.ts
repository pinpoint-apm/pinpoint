import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import type { ConfigMenu } from '../Layout/LayoutWithConfiguration';

export const SERVICE_CONFIG_MENU: ConfigMenu = {
  title: 'Service',
  desc: 'Manage services.',
  menus: [
    {
      name: 'Alarm',
      // Webhook은 자기 메뉴 항목이 없다. Alarm 화면의 탭으로만 오가므로 그 경로에서도
      // 이 항목이 선택된 것으로 보여야 한다(servermap 시절 Configuration 메뉴와 같은 규칙).
      path: [APP_PATH.CONFIG_SERVICE_ALARM, APP_PATH.CONFIG_SERVICE_WEBHOOK],
      href: APP_PATH.CONFIG_SERVICE_ALARM,
    },
  ],
};

export const isServiceConfigPath = (pathname: string) =>
  pathname === APP_PATH.CONFIG_SERVICE_SETTING ||
  pathname === APP_PATH.CONFIG_SERVICE_ALARM ||
  pathname === APP_PATH.CONFIG_SERVICE_WEBHOOK;
