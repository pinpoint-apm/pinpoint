import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { IMAGE_PATH, APP_PATH, APP_SETTING_KEYS, MenuItem } from '@pinpoint-fe/constants';
import {
  Menu,
  MenuItem as MenuItemComponent,
  SIDEBAR_COLLAPSED_WIDTH,
  SIDEBAR_WIDTH,
  Sidebar,
  SubMenu,
  bottomMenuItemStyles,
  menuItemStyles,
} from '../Sidebar';
import { cn } from '../../lib';
import {
  GlobalSearch,
  Separator,
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '..';
import { useDebounce, useHover } from 'usehooks-ts';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { LuCommand } from 'react-icons/lu';
import { useLocalStorage } from '@pinpoint-fe/hooks';
import { useAtom } from 'jotai';
import { globalSearchDisplayAtom } from '@pinpoint-fe/atoms';

export type SideNavigationMenuItem = MenuItem & {
  aHref?: string;
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
  content: React.ReactNode;
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
  const [, setGlobalSearchOpen] = useAtom(globalSearchDisplayAtom);
  const [collapsed] = useLocalStorage(APP_SETTING_KEYS.SIDE_NAV_BAR_SCALE, false);
  const { pathname } = useLocation();

  const SubMenuItem = ({ item }: MenuItemProps) => {
    const hoverRef = React.useRef<HTMLLIElement>(null);
    const subMenuContentElement =
      hoverRef.current?.querySelector<HTMLDivElement>('.ps-submenu-content');
    const isHover = useDebounce(useHover(hoverRef), 150);
    const isActive = item.childItems?.some(({ href }) => {
      if (href) {
        return pathname.includes(href);
      }
      return false;
    });
    const [open, setOpen] = React.useState(isActive);

    React.useEffect(() => {
      if (collapsed && subMenuContentElement) {
        // setOpen(false);
        if (isHover) {
          // setOpen(true);
          subMenuContentElement.style.visibility = 'visible';
          subMenuContentElement.style.marginLeft = '6px';
        } else {
          // setOpen(false);
          subMenuContentElement.style.visibility = 'hidden';
        }
      }
    }, [isHover, collapsed]);

    React.useEffect(() => {
      if (collapsed) {
        setOpen(false);
      }
      if (!collapsed && subMenuContentElement) {
        subMenuContentElement.style.visibility = 'visible';
        if (isActive) {
          setOpen(true);
        }
      }
    }, [collapsed, isActive]);

    return (
      <SubMenu
        className={cn({ hidden: item.hide }, 'mb-1')}
        ref={hoverRef}
        label={item.name}
        icon={item.icon && <span className="text-lg">{item.icon}</span>}
        active={isActive}
        open={open}
        onClick={(e) => {
          if (collapsed) {
            e.stopPropagation();
            e.preventDefault();
            return;
          } else {
            setOpen(!open);
          }
        }}
      >
        {collapsed && (
          <>
            <MenuItemComponent
              className={cn('pointer-events-none bg-[var(--snb-submenu-background)]', {
                'font-semibold opacity-100': isActive,
              })}
            >
              {item.name}
            </MenuItemComponent>
            <Separator className="mb-1 opacity-50" />
          </>
        )}
        {open && <div className="mb-1" />}
        <div className={cn('px-1 pb-1')}>
          {item.childItems?.map((childItem) => MenuItem({ item: childItem, className: 'text-sm' }))}
        </div>
      </SubMenu>
    );
  };

  const MenuItem = ({ item, className }: MenuItemProps) => {
    const isActive = Array.isArray(item.path)
      ? item.path.includes(pathname)
      : pathname.startsWith(item.path);
    return (
      <MenuItemComponent
        component={item.aHref ? <a href={item.aHref} /> : <Link to={`${item.href}`} />}
        className={cn('mb-0.5', { '!hidden': item.hide }, className)}
        active={isActive}
        icon={item.icon && <span className="text-lg">{item.icon}</span>}
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
          <Menu menuItemStyles={menuItemStyles} className="px-2">
            {WithTooltip({
              trigger: (
                <MenuItemComponent
                  component={<a>se</a>}
                  className={cn('mb-0.5 group/global_search')}
                  icon={<RxMagnifyingGlass className="text-lg" />}
                  onClick={() => setGlobalSearchOpen(true)}
                >
                  <div className="flex">
                    Search...
                    <div className="items-center hidden gap-1 px-1 ml-auto text-xs border rounded group-hover/global_search:flex bg-muted/25">
                      <LuCommand />K{/* <span className="text-xxs">ctrl</span> K */}
                    </div>
                  </div>
                </MenuItemComponent>
              ),
              content: (
                <div className="flex">
                  Search...
                  <div className="flex items-center gap-1 px-1 ml-3 text-xs border rounded bg-muted/25">
                    <LuCommand /> K
                  </div>
                </div>
              ),
            })}
          </Menu>
          <div className="px-4 my-2">
            <Separator className="opacity-50 " />
          </div>
          <Menu menuItemStyles={menuItemStyles} className="px-2">
            {topMenuItems?.map((item) => {
              return (
                <React.Fragment key={getMenuKey(item.path)}>
                  {WithTooltip({ trigger: MenuItem({ item }), content: item.name || '' })}
                </React.Fragment>
              );
            })}
          </Menu>
          <Menu menuItemStyles={bottomMenuItemStyles} className="px-2 mt-auto space-y-1">
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
      <GlobalSearch services={topMenuItems} />
    </div>
  );
};

const getMenuKey = (path: SideNavigationMenuItem['path']) => {
  return Array.isArray(path) ? path[0] : path;
};

export const getLayoutWithSideNavigation = (page: React.ReactNode) => {
  return <LayoutWithSideNavigation>{page}</LayoutWithSideNavigation>;
};
