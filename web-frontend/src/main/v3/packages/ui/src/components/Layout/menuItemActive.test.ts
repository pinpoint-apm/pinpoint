import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { findActiveConfigMenuGroup, findActiveMenuItem, isMenuItemActive } from './menuItemActive';
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
        leftChildItems: [item({ path: [APP_PATH.CONFIG_ALARM, APP_PATH.CONFIG_WEBHOOK] })],
      });

      expect(isMenuItemActive(service, '/config/alarm')).toBe(true);
      expect(isMenuItemActive(service, '/config/webhook')).toBe(true);
    });

    // service 단위로 갈리는 설정 화면은 경로가 하나뿐이라 두 그룹의 목록에 같은 경로가 들어
    // 있다. 감춘 쪽까지 세면 `/config/alarm`에서 두 그룹이 동시에 켜진다.
    test('ignores a hidden child', () => {
      const configuration = item({
        path: APP_PATH.CONFIG_USER_GROUP,
        childItems: [
          item({ path: APP_PATH.CONFIG_USER_GROUP }),
          item({ path: [APP_PATH.CONFIG_ALARM, APP_PATH.CONFIG_WEBHOOK], hide: true }),
        ],
      });

      expect(isMenuItemActive(configuration, '/config/alarm')).toBe(false);
      expect(isMenuItemActive(configuration, '/config/userGroup')).toBe(true);
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

// 화면이 어느 그룹 안에 그려지는지(`findActiveConfigMenuGroup`)와 사이드바에서 어느 그룹이
// 켜지는지(`findActiveMenuItem`)는 **같은 규칙**이어야 한다. 갈리면 화면은 Configuration 안에
// 그려지는데 사이드바는 아무것도 켜지지 않는다.
describe('visible-first, hidden as a fallback', () => {
  const ALARM_PATHS = [APP_PATH.CONFIG_ALARM, APP_PATH.CONFIG_WEBHOOK];

  const configuration = {
    title: 'Configuration',
    menus: [
      { name: 'User Group', path: APP_PATH.CONFIG_USER_GROUP, href: APP_PATH.CONFIG_USER_GROUP },
      { name: 'Alarms', path: ALARM_PATHS, href: APP_PATH.CONFIG_ALARM, hide: true },
      { name: 'Gated', path: '/config/gated', href: '/config/gated', hide: true },
    ],
  };
  const service = {
    title: 'Service',
    menus: [{ name: 'Alarm', path: ALARM_PATHS, href: APP_PATH.CONFIG_ALARM }],
  };
  const groups = [configuration, service];

  const configurationItem = item({
    name: 'Configuration',
    path: APP_PATH.CONFIG_USER_GROUP,
    childItems: configuration.menus.map(({ path, hide }) => item({ path, hide })),
  });
  const serviceItem = item({
    name: 'Service',
    path: APP_PATH.CONFIG_SERVICES,
    childItems: [item({ name: 'DEFAULT', path: `${APP_PATH.CONFIG_SERVICES}#DEFAULT` })],
    leftChildItems: service.menus.map(({ path }) => item({ path })),
  });
  const items = [configurationItem, serviceItem];

  // 같은 경로를 두 그룹이 나눠 가진다. 감추지 않은 쪽이 주인이다.
  test('the group that did not hide the entry owns a shared path', () => {
    expect(findActiveConfigMenuGroup(groups, '/config/alarm')).toBe(service);
    expect(findActiveMenuItem(items, '/config/alarm')).toBe(serviceItem);
  });

  // 기능 설정으로 메뉴에서 빠졌지만 URL로는 들어올 수 있는 화면. 아무도 안 켜지면
  // 제목도 메뉴도 없는 빈 레이아웃이 되므로, 그때만 감춘 항목까지 본다.
  test('falls back to the hidden entry when nothing visible matches', () => {
    expect(findActiveConfigMenuGroup(groups, '/config/gated')).toBe(configuration);
    expect(findActiveMenuItem(items, '/config/gated')).toBe(configurationItem);
  });

  test('finds nothing for a path no group lists', () => {
    expect(findActiveConfigMenuGroup(groups, '/config/unknown')).toBeUndefined();
    expect(findActiveMenuItem(items, '/config/unknown')).toBeUndefined();
  });
});

// 라우터는 끝에 '/'가 붙어도 같은 화면을 매칭한다. 경로를 그대로 비교하면 그런 링크·북마크에서
// 어느 메뉴도 주인이 되지 못해, 제목도 메뉴도 탭 내용도 없는 화면이 나온다.
describe('a trailing slash', () => {
  const alarms = item({ path: [APP_PATH.CONFIG_ALARM, APP_PATH.CONFIG_WEBHOOK] });
  const inspector = item({ path: APP_PATH.INSPECTOR });

  test('does not change which menu owns the path', () => {
    expect(isMenuItemActive(alarms, '/config/alarm/')).toBe(true);
    expect(isMenuItemActive(inspector, '/inspector/')).toBe(true);
  });

  test('does not make an unrelated menu match', () => {
    expect(isMenuItemActive(alarms, '/config/alarmed')).toBe(false);
  });
});

// 감춘 항목은 그려지지도 않는다. 첫 단계에서 세면 `findActiveMenuItem`이 그것을 고르고 멈춰,
// 같은 경로를 맡는 보이는 항목이 가려져 사이드바에 아무것도 켜지지 않는다.
describe('a hidden top-level item', () => {
  const hidden = item({ name: 'Servermap', path: APP_PATH.SERVER_MAP, hide: true });
  const visible = item({ name: 'Inspector', path: APP_PATH.INSPECTOR });
  const items = [hidden, visible];

  test('is not active on its own path', () => {
    expect(isMenuItemActive(hidden, '/serverMap')).toBe(false);
  });

  test('is picked only by the fallback pass', () => {
    expect(findActiveMenuItem(items, '/serverMap')).toBe(hidden);
    expect(findActiveMenuItem(items, '/inspector')).toBe(visible);
  });
});
