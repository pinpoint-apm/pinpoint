import type { SideNavigationMenuItem } from './LayoutWithSideNavigation';

/**
 * 메뉴 항목의 경로가 지금 경로인지. 배열이면 그중 하나와 정확히 같아야 하고, 문자열이면
 * 하위 경로까지 맞다고 본다(`/inspector`가 `/inspector/myApp@TOMCAT`도 맡는다).
 */
const matchesPathname = (path: SideNavigationMenuItem['path'], pathname: string) =>
  Array.isArray(path) ? path.includes(pathname) : pathname.startsWith(path);

/**
 * 사이드바에서 이 메뉴 항목을 켜 둘지 여부.
 *
 * 자식이 켜지면 부모도 켜진다. 그런데 자식은 **두 갈래**다 — 드롭다운 오른쪽 칸의
 * `childItems`와 왼쪽 칸에 따로 그리는 `leftChildItems`. 둘 다 봐야 한다.
 *
 * Service 그룹이 그렇다: 오른쪽은 service 목록(`childItems`)이고 왼쪽이 그 service의 설정
 * 화면들(`leftChildItems`)이다. 그런데 오른쪽 목록의 path는 `/config/service#<name>`이라 어떤
 * 경로와도 맞지 않아, 왼쪽을 빠뜨리면 `/config/service/...`에 있어도 그룹이 영영 꺼져 있었다.
 *
 * 자식이 있어도 **자기 경로를 마저 본다.** 자식 목록에 없는 하위 화면(Service 그룹의
 * `/config/serviceSetting`)에서도 그룹은 켜져 있어야 한다.
 */
export const isMenuItemActive = (item: SideNavigationMenuItem, pathname: string): boolean => {
  const childItems = [...(item.childItems ?? []), ...(item.leftChildItems ?? [])];

  if (childItems.some(({ path }) => matchesPathname(path, pathname))) {
    return true;
  }

  return matchesPathname(item.path, pathname);
};
