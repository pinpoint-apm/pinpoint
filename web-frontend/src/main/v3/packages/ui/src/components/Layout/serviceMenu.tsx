import { LuCheck } from 'react-icons/lu';
import { APP_PATH, GetServices } from '@pinpoint-fe/ui/src/constants';
import type { SideNavigationMenuItem } from '@pinpoint-fe/ui';

export const buildServiceAsideMenus = (services: GetServices.Response | undefined) =>
  (services ?? []).map((name) => ({
    name,
    path: APP_PATH.CONFIG_SERVICES,
    href: APP_PATH.CONFIG_SERVICES,
  }));

export const buildServiceSidebarItems = (
  services: GetServices.Response | undefined,
  selectedService: string,
  onSelect: (name: string) => void,
): SideNavigationMenuItem[] =>
  (services ?? []).map((name) => {
    const isSelected = selectedService === name;
    return {
      name,
      path: `${APP_PATH.CONFIG_SERVICES}#${name}`,
      onClick: () => onSelect(name),
      selected: isSelected,
      children: (
        <span className="flex items-center justify-between gap-2 w-full">
          <span className="truncate">{name}</span>
          {isSelected && (
            <LuCheck className="shrink-0 w-6 h-6 opacity-100 text-white" strokeWidth={4} />
          )}
        </span>
      ),
    };
  });
