export type MenuItemType = {
  name: string;
  path: string | string[];
  href?: string;
  icon?: React.ReactElement;
  hide?: boolean;
};
