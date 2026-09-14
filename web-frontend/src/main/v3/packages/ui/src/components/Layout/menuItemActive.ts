import { withoutTrailingSlash } from '@pinpoint-fe/ui/src/utils';
import type { ConfigMenu } from './LayoutWithConfiguration';
import type { SideNavigationMenuItem } from './LayoutWithSideNavigation';

/**
 * 메뉴 항목의 경로가 지금 경로인지. 배열이면 그중 하나와 정확히 같아야 하고, 문자열이면
 * 하위 경로까지 맞다고 본다(`/inspector`가 `/inspector/myApp@TOMCAT`도 맡는다).
 *
 * 라우터는 `/config/alarm`과 `/config/alarm/`을 같은 화면으로 매칭하므로 끝의 '/'를 떼고
 * 비교한다. 안 떼면 '/'가 붙은 링크·북마크에서 어느 메뉴도 그 경로의 주인이 되지 못해
 * 제목도 메뉴도 없는 화면이 나온다.
 */
const matchesPathname = (path: SideNavigationMenuItem['path'], rawPathname: string) => {
  const pathname = withoutTrailingSlash(rawPathname);

  return Array.isArray(path) ? path.includes(pathname) : pathname.startsWith(path);
};

/**
 * **감춘 항목은 그 경로의 주인이 아니다.** service 단위로 갈리는 설정 화면들은 경로가 하나뿐이라
 * Configuration 그룹과 Service 그룹 양쪽 목록에 같은 경로가 들어 있고, 감추지 않은 쪽이 주인이다.
 * 감춘 것까지 한 번에 세면 `/config/alarm`에서 두 그룹이 동시에 켜진다.
 *
 * `includeHidden`은 그래도 주인을 못 찾았을 때의 두 번째 단계다 — 아래
 * `findActiveMenuItem`·`findActiveConfigMenuGroup` 참고.
 */
const matchesMenu = (
  menu: { path: SideNavigationMenuItem['path']; hide?: boolean },
  pathname: string,
  includeHidden: boolean,
) => (menu.hide && !includeHidden ? false : matchesPathname(menu.path, pathname));

/**
 * 사이드바에서 이 메뉴 항목을 켜 둘지 여부.
 *
 * 자식이 켜지면 부모도 켜진다. 그런데 자식은 **두 갈래**다 — 드롭다운 오른쪽 칸의
 * `childItems`와 왼쪽 칸에 따로 그리는 `leftChildItems`. 둘 다 봐야 한다.
 *
 * Service 그룹이 그렇다: 오른쪽은 service 목록(`childItems`)이고 왼쪽이 그 service의 설정
 * 화면들(`leftChildItems`)이다. 그런데 오른쪽 목록의 path는 `/config/service#<name>`이라 어떤
 * 경로와도 맞지 않아, 왼쪽을 빠뜨리면 그 설정 화면들에 있어도 그룹이 영영 꺼져 있었다.
 *
 * 자식이 있어도 **자기 경로를 마저 본다.** 자식 목록에 없는 하위 화면(Service 그룹의
 * `/config/serviceSetting`)에서도 그룹은 켜져 있어야 한다.
 */
export const isMenuItemActive = (
  item: SideNavigationMenuItem,
  pathname: string,
  { includeHidden = false }: { includeHidden?: boolean } = {},
): boolean => {
  // 감춘 항목은 그려지지도 않는다. 첫 단계에서 세면 같은 경로를 맡는 보이는 항목을 가로채
  // (`findActiveMenuItem`이 그것을 고르고 멈춘다) 아무것도 켜지지 않는다.
  if (item.hide && !includeHidden) {
    return false;
  }

  const childItems = [...(item.childItems ?? []), ...(item.leftChildItems ?? [])];

  if (childItems.some((childItem) => matchesMenu(childItem, pathname, includeHidden))) {
    return true;
  }

  return matchesPathname(item.path, pathname);
};

/**
 * 이 경로의 주인인 메뉴 항목. **보이는 항목을 먼저 보고, 없으면 감춘 항목까지 보고 다시 찾는다.**
 *
 * 두 단계인 이유는 기능 설정으로 메뉴에서 빠진 화면 때문이다. 그런 화면도 URL로 들어오는 길은
 * 남아 있고, 들어오면 화면은 원래 속한 그룹 안에서 그려진다 — 사이드바도 같은 그룹을 켜야 한다.
 * 그렇다고 첫 단계부터 감춘 항목을 세면, 같은 경로를 나눠 가진 두 그룹이 동시에 켜진다.
 *
 * 설정 화면 쪽 `findActiveConfigMenuGroup`과 **같은 규칙이다.** 둘이 갈리면 화면은 어느 그룹
 * 안에 그려지는데 사이드바는 그 그룹을 켜지 않는 어긋남이 생긴다.
 */
export const findActiveMenuItem = (items: SideNavigationMenuItem[], pathname: string) =>
  items.find((item) => isMenuItemActive(item, pathname)) ??
  items.find((item) => isMenuItemActive(item, pathname, { includeHidden: true }));

/**
 * 이 경로가 어느 설정 메뉴 그룹의 것인지. 규칙은 `findActiveMenuItem`과 같다 —
 * 보이는 항목이 먼저고, 없으면 감춘 항목까지 보고 다시 찾는다.
 */
export const findActiveConfigMenuGroup = <T extends ConfigMenu>(groups: T[], pathname: string) => {
  const owns = (group: T, includeHidden: boolean) =>
    group.menus.some((menu) => matchesMenu(menu, pathname, includeHidden));

  return groups.find((group) => owns(group, false)) ?? groups.find((group) => owns(group, true));
};
