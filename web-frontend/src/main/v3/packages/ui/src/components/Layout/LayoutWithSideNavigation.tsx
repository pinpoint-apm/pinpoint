import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { IMAGE_PATH, APP_PATH } from '@pinpoint-fe/constants';
import {
  Menu,
  MenuItem as MenuItemComponent,
  SIDEBAR_COLLAPSED_WIDTH,
  SIDEBAR_WIDTH,
  Sidebar,
  SubMenu,
  bottomMenuItemStyles,
  menuItemStyles,
  useProSidebar,
} from '../Sidebar';
import { cn } from '../../lib';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '..';
import { useDebounce, useHover } from 'usehooks-ts';

export type SideNavigationMenuItem = {
  hide?: boolean;
  path: string | string[];
  name?: string;
  href?: string;
  aHref?: string;
  icon?: React.ReactNode;
  children?: React.ReactNode | ((collapsed: boolean) => React.ReactNode);
  childItems?: SideNavigationMenuItem[];
  onClick?: () => void;
};

interface MenuItemProps {
  item: SideNavigationMenuItem;
  className?: string;
}

interface TooltipProps {
  trigger: React.JSX.Element;
  content: string;
}

export interface LayoutWithSideNavigationProps {
  children: React.ReactNode;
  topMenuItems?: SideNavigationMenuItem[];
  bottomMenuItems?: SideNavigationMenuItem[];
}

export const LayoutWithSideNavigation = ({
  children,
  topMenuItems,
  bottomMenuItems,
}: LayoutWithSideNavigationProps) => {
  const { collapsed } = useProSidebar();
  const { pathname } = useLocation();

  const SubMenuItem = ({ item }: MenuItemProps) => {
    const hoverRef = React.useRef<HTMLLIElement>(null);
    const subMenuContentElement =
      hoverRef.current?.querySelector<HTMLDivElement>('.ps-submenu-content');
    const isHover = useDebounce(useHover(hoverRef), 150);
    const isActive = item.childItems?.some(({ href = '' }) => pathname.includes(href));

    React.useEffect(() => {
      if (collapsed && subMenuContentElement) {
        subMenuContentElement.style.visibility = isHover ? 'visible' : 'hidden';
      }
    }, [isHover, collapsed]);

    React.useEffect(() => {
      if (!collapsed && subMenuContentElement) {
        subMenuContentElement.style.visibility = 'visible';
      }
    }, [collapsed]);

    return (
      <SubMenu
        className={`${item.hide && 'hidden'}`}
        ref={hoverRef}
        label={item.name}
        icon={item.icon}
        active={isActive}
        defaultOpen={isActive}
        onClick={(e) => {
          if (collapsed) {
            e.stopPropagation();
            e.preventDefault();
            return;
          }
        }}
      >
        {collapsed && (
          <MenuItemComponent className="bg-[var(--blue-800)] pointer-events-none" active={isActive}>
            {item.name}
          </MenuItemComponent>
        )}
        {item.childItems?.map((childItem) => MenuItem({ item: childItem, className: 'text-sm' }))}
      </SubMenu>
    );
  };

  const MenuItem = ({ item, className }: MenuItemProps) => {
    return (
      <MenuItemComponent
        component={item.aHref ? <a href={item.aHref} /> : <Link to={`${item.href}`} />}
        className={cn({ '!hidden': item.hide }, className)}
        active={
          Array.isArray(item.path) ? item.path.includes(pathname) : pathname.startsWith(item.path)
        }
        icon={item.icon}
        key={getMenuKey(item.path)}
      >
        {item.children
          ? typeof item.children === 'function'
            ? item.children(collapsed)
            : item.children
          : item.name}
      </MenuItemComponent>
    );
  };

  const WithTooltip = ({ trigger, content }: TooltipProps) => {
    return (
      <Tooltip>
        <TooltipTrigger asChild>{trigger}</TooltipTrigger>
        <TooltipContent side="right" className={cn({ hidden: !collapsed })}>
          <p>{content}</p>
        </TooltipContent>
      </Tooltip>
    );
  };

  return (
    <div className="flex h-screen">
      <Sidebar
        header={(collapsed) => {
          return (
            <Link to={APP_PATH.SERVER_MAP}>
              <img
                src={collapsed ? `${IMAGE_PATH}/mini-logo.png` : `${IMAGE_PATH}/logo.png`}
                alt={'pinpoint-logo'}
              />
            </Link>
          );
        }}
      >
        <TooltipProvider delayDuration={100}>
          <Menu menuItemStyles={menuItemStyles}>
            {topMenuItems?.map((item) => {
              return (
                <React.Fragment key={getMenuKey(item.path)}>
                  {WithTooltip({ trigger: MenuItem({ item }), content: item.name || '' })}
                </React.Fragment>
              );
            })}
          </Menu>
          <Menu style={{ marginTop: 'auto' }} menuItemStyles={bottomMenuItemStyles}>
            {bottomMenuItems?.map((item) => {
              return (
                <React.Fragment key={getMenuKey(item.path)}>
                  {item.childItems
                    ? SubMenuItem({ item })
                    : WithTooltip({ trigger: MenuItem({ item }), content: item.name || '' })}
                </React.Fragment>
              );
            })}
          </Menu>
        </TooltipProvider>
      </Sidebar>
      <div
        style={{
          width: `calc(100% - ${collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_WIDTH}px)`,
          height: '100%',
        }}
      >
        {children}
      </div>
    </div>
  );
};

const getMenuKey = (path: SideNavigationMenuItem['path']) => {
  return Array.isArray(path) ? path[0] : path;
};

export const getLayoutWithSideNavigation = (page: React.ReactNode) => {
  return <LayoutWithSideNavigation>{page}</LayoutWithSideNavigation>;
};
