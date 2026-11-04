import type { MenuItemType } from '@pinpoint-fe/ui/src/constants';

/**
 * 드롭다운 오른쪽 칸 목록을 검색어로 거른다. 이름의 일부만 맞아도 남기고, 대소문자와 앞뒤 공백은
 * 따지지 않는다. 순서는 바꾸지 않는다(선택된 항목이 맨 위에 오는 정렬을 그대로 둔다).
 */
export const filterChildItemsByName = <T extends Pick<MenuItemType, 'name'>>(
  childItems: T[],
  query: string,
): T[] => {
  const keyword = query.trim().toLowerCase();
  if (!keyword) {
    return childItems;
  }
  return childItems.filter((childItem) => childItem.name.toLowerCase().includes(keyword));
};
