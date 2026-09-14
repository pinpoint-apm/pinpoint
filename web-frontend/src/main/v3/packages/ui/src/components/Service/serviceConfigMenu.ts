import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import type { ConfigMenu } from '../Layout/LayoutWithConfiguration';

/**
 * service 단위로 갈리는 설정 화면들. **경로는 servermap 시절과 같다** — 화면마다 경로는
 * 하나뿐이고, `enableServiceMap`에 따라 이 그룹에 놓이느냐 Configuration 그룹에 놓이느냐만
 * 달라진다. 그래서 경로만 보고 그룹을 정할 수 없고, 두 그룹 중 그 항목을 **감추지 않은**
 * 쪽이 그 경로의 그룹이다(`hide`).
 */
export const SERVICE_CONFIG_MENU: ConfigMenu = {
  title: 'Service',
  desc: 'Manage services.',
  menus: [
    {
      name: 'Alarm',
      // Webhook은 자기 메뉴 항목이 없다. Alarm 화면의 탭으로만 오가므로 그 경로에서도
      // 이 항목이 선택된 것으로 보여야 한다.
      path: [APP_PATH.CONFIG_ALARM, APP_PATH.CONFIG_WEBHOOK],
      href: APP_PATH.CONFIG_ALARM,
    },
  ],
};
