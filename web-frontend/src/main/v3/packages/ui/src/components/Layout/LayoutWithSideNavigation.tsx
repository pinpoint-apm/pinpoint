import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import {
  IMAGE_PATH,
  APP_PATH,
  APP_SETTING_KEYS,
  MenuItemType as MenuItem,
} from '@pinpoint-fe/ui/src/constants';
import { cn } from '../../lib';
import {
  ErrorBoundary,
  GlobalSearch,
  Separator,
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '..';
import { LuChevronRight, LuChevronFirst, LuChevronLast } from 'react-icons/lu';
import { RxMagnifyingGlass } from 'react-icons/rx';
import { LuCommand } from 'react-icons/lu';
import { useLocalStorage } from '@pinpoint-fe/ui/src/hooks';
import { useAtom } from 'jotai';
import { globalSearchDisplayAtom } from '@pinpoint-fe/ui/src/atoms';
import {
  SidebarProvider,
  SidebarMenuItem,
  SidebarMenuButton,
  Sidebar as ShadcnUiSidebar,
  SidebarContent,
  SidebarMenu,
  SidebarHeader,
  SidebarFooter,
  SidebarInset,
} from '@pinpoint-fe/ui/src/components/ui/sidebar';
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@pinpoint-fe/ui/src/components/ui/dropdown-menu';

const SIDEBAR_WIDTH = 200;
const SIDEBAR_COLLAPSED_WIDTH = 64;

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
  trigger: React.ReactNode;
  content: React.ReactNode;
  hidden?: boolean;
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
  const [collapsed, setCollapsed] = useLocalStorage(APP_SETTING_KEYS.SIDE_NAV_BAR_SCALE, false);
  const { pathname } = useLocation();

  const isActive = React.useCallback(
    (item: SideNavigationMenuItem) => {
      if (item?.childItems && item?.childItems?.length > 0) {
        return item?.childItems?.some(({ path }) => {
          return Array.isArray(path) ? path.includes(pathname) : pathname.startsWith(path);
        });
      }
      return Array.isArray(item.path)
        ? item.path.includes(pathname)
        : pathname.startsWith(item.path);
    },
    [pathname],
  );

  const renderMenuItemContent = (item: SideNavigationMenuItem, isChildItem?: boolean) => {
    const itemClassName = cn('w-full', { '!hidden': item.hide }, { 'justify-center': collapsed });

    const itemChildren = (
      <div className="flex items-center w-full">
        {item?.icon && (
          <span
            className={cn(
              'text-lg mr-1 transition-opacity w-[35px] h-[35px] flex items-center justify-center relative',
              isActive(item) ? 'opacity-100' : 'opacity-70',
            )}
          >
            {item.icon}
            {collapsed && item.childItems && item.childItems.length > 0 && (
              <span className="absolute top-2 right-1 w-1.5 h-1.5 bg-white rounded-full" />
            )}
          </span>
        )}
        {collapsed && !isChildItem ? null : (
          <>
            <span
              className={cn(
                'transition-opacity truncate',
                isActive(item) ? 'opacity-100' : 'opacity-70',
                {
                  'pl-6 pr-2': !item?.icon,
                },
              )}
            >
              {item.children
                ? typeof item.children === 'function'
                  ? item.children(collapsed)
                  : item.children
                : item.name}
            </span>
            {item.childItems && item.childItems?.length > 0 && (
              <LuChevronRight className="ml-auto" />
            )}
          </>
        )}
      </div>
    );

    if (item?.childItems && item?.childItems?.length > 0) {
      return <div className="w-full">{itemChildren}</div>;
    } else if (item.aHref) {
      return (
        <a href={item.aHref} className={itemClassName}>
          {itemChildren}
        </a>
      );
    } else {
      return (
        <Link to={`${item.href}`} className={itemClassName}>
          {itemChildren}
        </Link>
      );
    }
  };

  const renderSidebarMenuItem = (item: SideNavigationMenuItem) => {
    const sidebarMenuButtonClassName = cn(
      '!h-10 !rounded p-1.5 !pl-1.5 !pr-2 text-sm',
      'hover:bg-[var(--blue-700)] hover:text-[var(--white-default)]',
      'focus:bg-[var(--blue-700)] focus:text-[var(--white-default)]',
    );

    if (item.childItems) {
      return (
        <SidebarMenuItem>
          <DropdownMenu>
            {WithTooltip({
              trigger: (
                <DropdownMenuTrigger asChild>
                  <SidebarMenuButton
                    className={cn(sidebarMenuButtonClassName, {
                      'bg-[var(--blue-700)] font-semibold': isActive(item),
                    })}
                  >
                    {renderMenuItemContent(item)}
                  </SidebarMenuButton>
                </DropdownMenuTrigger>
              ),
              content: item.name || '',
              hidden: !collapsed,
            })}
            <DropdownMenuContent
              side="right"
              align="start"
              sideOffset={15}
              alignOffset={-50}
              className="bg-[var(--blue-900)] border-none rounded-md min-w-[180px] shadow-lg text-[var(--white-default)] w-full"
            >
              <div className="opacity-50 h-10 flex items-center pl-6 pr-2 text-sm">{item.name}</div>
              <Separator className="opacity-50 mb-2" />
              <div className="space-y-1">
                {item?.childItems?.map((childItem) => {
                  return (
                    <DropdownMenuItem
                      key={getMenuKey(childItem.path)}
                      className={cn(
                        sidebarMenuButtonClassName,
                        {
                          'bg-[var(--blue-700)] font-semibold': isActive(childItem),
                        },
                        'cursor-pointer',
                      )}
                      asChild
                    >
                      {renderMenuItemContent(childItem, true)}
                    </DropdownMenuItem>
                  );
                })}
              </div>
            </DropdownMenuContent>
          </DropdownMenu>
        </SidebarMenuItem>
      );
    }

    return WithTooltip({
      trigger: (
        <SidebarMenuButton
          className={cn(sidebarMenuButtonClassName, {
            'bg-[var(--blue-700)] font-semibold': isActive(item),
          })}
        >
          {renderMenuItemContent(item)}
        </SidebarMenuButton>
      ),
      content: item.name || '',
      hidden: !collapsed,
    });
  };

  return (
    <SidebarProvider
      style={
        {
          '--sidebar-width': `${collapsed ? SIDEBAR_COLLAPSED_WIDTH : SIDEBAR_WIDTH}px`,
        } as React.CSSProperties
      }
      className="h-screen"
    >
      <ErrorBoundary
        fallbackRender={({ error }) => (
          <div className="flex flex-col items-center justify-center w-[200px] h-full gap-5 p-3">
            <div className="w-full text-center max-w-[28rem]">
              <p className="mb-2 text-sm truncate">{error?.message}</p>
            </div>
          </div>
        )}
      >
        <React.Fragment>
          <TooltipProvider delayDuration={100}>
            <ShadcnUiSidebar
              style={{
                color: 'var(--snb-text)',
                background: 'var(--snb-background)',
              }}
              className="[&:hover_.scale-button-wrapper]:inline-block"
            >
              <SidebarHeader className="gap-0 p-0">
                <div className="mb-2">
                  <div
                    className={cn(
                      'relative h-16 min-h-[4rem] flex items-center pl-6 hover:bg-[--blue-700]',
                      { 'justify-center pl-0': collapsed },
                    )}
                  >
                    <Link to={APP_PATH.SERVER_MAP} className="flex items-center">
                      <img
                        src={collapsed ? `${IMAGE_PATH}/mini-logo.png` : `${IMAGE_PATH}/logo.png`}
                        alt={'pinpoint-logo'}
                      />
                    </Link>
                    <div className="absolute hidden scale-button-wrapper top-1 right-1">
                      <button
                        className="flex items-center justify-center w-6 h-6 text-white opacity-50 cursor-pointer hover:opacity-100 hover:font-semibold"
                        onClick={() => setCollapsed(!collapsed)}
                      >
                        {collapsed ? <LuChevronLast /> : <LuChevronFirst />}
                      </button>
                    </div>
                  </div>
                </div>

                <SidebarMenuItem className="px-2">
                  {WithTooltip({
                    trigger: (
                      <SidebarMenuButton
                        className={cn(
                          'mb-0.5 group/global_search group/search-item !h-10 !rounded !p-0 !pl-1.5 !pr-2 text-sm',
                          'bg-transparent hover:bg-[--blue-700] transition-colors',
                        )}
                        onClick={() => setGlobalSearchOpen(true)}
                      >
                        <div className="flex items-center w-full">
                          <span className="w-[35px] h-[35px] flex items-center justify-center">
                            <RxMagnifyingGlass className="mr-1 text-lg text-white/70" />
                          </span>
                          {collapsed ? null : (
                            <>
                              <span className="text-white/70">Search...</span>
                              <div className="items-center hidden gap-1 px-1.5 ml-auto text-xs border border-white/20 rounded group-hover/global_search:flex bg-white/10 text-white">
                                <LuCommand className="text-xs" />K
                              </div>
                            </>
                          )}
                        </div>
                      </SidebarMenuButton>
                    ),
                    content: (
                      <div className="flex">
                        Search...
                        <div className="flex items-center gap-1 px-1 ml-3 text-xs border rounded bg-muted/25">
                          <LuCommand /> K
                        </div>
                      </div>
                    ),
                    hidden: !collapsed,
                  })}
                </SidebarMenuItem>
                <div className="px-4 my-2">
                  <Separator className="opacity-50 " />
                </div>
              </SidebarHeader>
              <SidebarContent>
                <SidebarMenu className="gap-1 px-2">
                  {topMenuItems?.map((item, index) => {
                    return (
                      <React.Fragment key={getMenuKey(item.path)}>
                        {renderSidebarMenuItem(item)}
                      </React.Fragment>
                    );
                  })}
                </SidebarMenu>
              </SidebarContent>
              <SidebarFooter className="px-0 pb-10">
                <SidebarMenu className="px-2">
                  {bottomMenuItems?.map((item) => {
                    return (
                      <React.Fragment key={getMenuKey(item.path)}>
                        {renderSidebarMenuItem(item)}
                      </React.Fragment>
                    );
                  })}
                </SidebarMenu>
              </SidebarFooter>
            </ShadcnUiSidebar>
          </TooltipProvider>
        </React.Fragment>
      </ErrorBoundary>
      <SidebarInset>
        <ErrorBoundary>{children}</ErrorBoundary>
      </SidebarInset>
      <GlobalSearch services={topMenuItems} />
    </SidebarProvider>
  );
};

const WithTooltip = ({ trigger, content, hidden }: TooltipProps) => {
  return (
    <Tooltip>
      <TooltipTrigger asChild>{trigger}</TooltipTrigger>
      <TooltipContent side="right" className={cn({ hidden: !!hidden })}>
        {content}
      </TooltipContent>
    </Tooltip>
  );
};

const getMenuKey = (path: SideNavigationMenuItem['path']) => {
  return Array.isArray(path) ? path[0] : path;
};

export const getLayoutWithSideNavigation = (page: React.ReactNode) => {
  return <LayoutWithSideNavigation>{page}</LayoutWithSideNavigation>;
};
