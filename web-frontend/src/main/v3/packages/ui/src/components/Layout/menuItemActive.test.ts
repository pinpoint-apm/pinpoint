import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { isMenuItemActive } from './menuItemActive';
import type { SideNavigationMenuItem } from './LayoutWithSideNavigation';

const item = (menuItem: Partial<SideNavigationMenuItem>): SideNavigationMenuItem => ({
  name: 'menu',
  path: '/none',
  ...menuItem,
});

describe('isMenuItemActive', () => {
  describe('a menu item without children', () => {
    test('is active on its own path and on paths under it', () => {
      const inspector = item({ path: APP_PATH.INSPECTOR });

      expect(isMenuItemActive(inspector, '/inspector')).toBe(true);
      expect(isMenuItemActive(inspector, '/inspector/myApp@TOMCAT')).toBe(true);
      expect(isMenuItemActive(inspector, '/serverMap')).toBe(false);
    });

    // 경로를 여러 개 맡는 항목은 정확히 일치할 때만 켜진다 — 하위 경로까지 맡지 않는다.
    test('with a list of paths matches them exactly', () => {
      const alarms = item({ path: [APP_PATH.CONFIG_ALARM, APP_PATH.CONFIG_WEBHOOK] });

      expect(isMenuItemActive(alarms, '/config/alarm')).toBe(true);
      expect(isMenuItemActive(alarms, '/config/webhook')).toBe(true);
      expect(isMenuItemActive(alarms, '/config/alarm/detail')).toBe(false);
    });
  });

  describe('a group with children', () => {
    // Configuration 그룹 — 자식 중 하나가 지금 경로면 그룹이 켜진다.
    test('is active when one of its childItems matches', () => {
      const configuration = item({
        path: APP_PATH.CONFIG_USER_GROUP,
        childItems: [
          item({ path: APP_PATH.CONFIG_USER_GROUP }),
          item({ path: APP_PATH.CONFIG_INSTALLATION }),
        ],
      });

      expect(isMenuItemActive(configuration, '/config/installation')).toBe(true);
      expect(isMenuItemActive(configuration, '/config/general')).toBe(false);
    });

    // Service 그룹이 꺼져 있던 원인. 오른쪽 칸(childItems)은 service 목록이라 어떤 경로와도
    // 맞지 않고, 실제 화면들은 왼쪽 칸(leftChildItems)에 있다.
    test('is active when one of its leftChildItems matches', () => {
      const service = item({
        path: APP_PATH.CONFIG_SERVICES,
        childItems: [
          item({ name: 'DEFAULT', path: `${APP_PATH.CONFIG_SERVICES}#DEFAULT` }),
          item({ name: 'myService', path: `${APP_PATH.CONFIG_SERVICES}#myService` }),
        ],
        leftChildItems: [
          item({ path: [APP_PATH.CONFIG_SERVICE_ALARM, APP_PATH.CONFIG_SERVICE_WEBHOOK] }),
        ],
      });

      expect(isMenuItemActive(service, '/config/service/alarm')).toBe(true);
      expect(isMenuItemActive(service, '/config/service/webhook')).toBe(true);
    });

    // 자식 목록에 없는 하위 화면이다. 자식이 있어도 자기 경로를 마저 봐야 켜진다.
    test('falls back to its own path when no child matches', () => {
      const service = item({
        path: APP_PATH.CONFIG_SERVICES,
        childItems: [item({ name: 'DEFAULT', path: `${APP_PATH.CONFIG_SERVICES}#DEFAULT` })],
      });

      expect(isMenuItemActive(service, '/config/serviceSetting')).toBe(true);
      expect(isMenuItemActive(service, '/config/alarm')).toBe(false);
    });
  });
});
