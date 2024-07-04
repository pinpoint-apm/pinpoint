export type MenuItem = {
  name: string;
  path: string | string[];
  href?: string;
  icon?: JSX.Element;
  hide?: boolean;
};
