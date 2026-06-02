import React from 'react';
import { TbCategory } from 'react-icons/tb';
import { LuPlus, LuEllipsis } from 'react-icons/lu';
import { useNavigate } from 'react-router-dom';
import { useAtomValue, useSetAtom } from 'jotai';
import {
  DEFAULT_SERVICE,
  isServiceAddSheetOpenAtom,
  selectedServiceAtom,
  servicesAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, Configuration } from '@pinpoint-fe/ui/src/constants';
import { buildServiceSidebarItems } from '../Layout/serviceMenu';
import { SERVICE_CONFIG_MENU } from './serviceConfigMenu';
import type { SideNavigationMenuItem } from '../Layout/LayoutWithSideNavigation';

export const useServiceSideNavigation = (
  configuration: Configuration | undefined,
  serviceGroupItems: SideNavigationMenuItem[] = [],
) => {
  const enableServiceMap = !!configuration?.['experimental.enableServiceMap.value'];
  const services = useAtomValue(servicesAtom);
  const selectedService = useAtomValue(selectedServiceAtom);
  const setSelectedService = useSetAtom(selectedServiceAtom);
  const setAddServiceOpen = useSetAtom(isServiceAddSheetOpenAtom);
  const navigate = useNavigate();

  React.useEffect(() => {
    if (!enableServiceMap || !services) return;
    if (!selectedService || !services.includes(selectedService)) {
      setSelectedService(DEFAULT_SERVICE);
    }
  }, [enableServiceMap, services, selectedService, setSelectedService]);

  const serviceMenuItems: SideNavigationMenuItem[] = enableServiceMap
    ? [
        {
          icon: <TbCategory />,
          name: SERVICE_CONFIG_MENU.title,
          path: APP_PATH.CONFIG_SERVICES,
          childItems: buildServiceSidebarItems(services, selectedService, setSelectedService),
          leftSectionTitle: 'Service Config',
          leftChildItems: serviceGroupItems,
          rightSectionTitle: 'Select Service',
          headerAction: (close: () => void) => (
            <div className="flex items-center gap-1">
              <button
                type="button"
                className="inline-flex items-center gap-1 px-2 py-0.5 text-xs font-medium rounded-md bg-[var(--blue-700)] text-white hover:bg-[var(--blue-600)]"
                onClick={(e) => {
                  e.stopPropagation();
                  close();
                  navigate(APP_PATH.CONFIG_SERVICE_SETTING);
                  setAddServiceOpen(true);
                }}
              >
                <LuPlus className="w-3.5 h-3.5" strokeWidth={3} />
                New
              </button>
              <button
                type="button"
                aria-label="Service Setting"
                title="Service Setting"
                className="inline-flex items-center justify-center w-6 h-6 rounded-md text-white hover:bg-[var(--blue-700)]"
                onClick={(e) => {
                  e.stopPropagation();
                  close();
                  navigate(APP_PATH.CONFIG_SERVICE_SETTING);
                }}
              >
                <LuEllipsis className="w-4 h-4" />
              </button>
            </div>
          ),
        },
      ]
    : [];

  return {
    serviceMenuItems,
  };
};
