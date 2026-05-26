import React from 'react';
import { TbCategory } from 'react-icons/tb';
import { LuPlus } from 'react-icons/lu';
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

export const useServiceSideNavigation = (configuration: Configuration | undefined) => {
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

  const serviceGroupItems: SideNavigationMenuItem[] = [
    {
      name: 'Service Setting',
      path: APP_PATH.CONFIG_SERVICE_SETTING,
      href: APP_PATH.CONFIG_SERVICE_SETTING,
    },
    {
      name: `User Group (${selectedService})`,
      path: APP_PATH.CONFIG_SERVICE_USER_GROUP,
      href: APP_PATH.CONFIG_SERVICE_USER_GROUP,
    },
    {
      name: `Alarm (${selectedService})`,
      path: APP_PATH.CONFIG_SERVICE_ALARM,
      href: APP_PATH.CONFIG_SERVICE_ALARM,
    },
  ];

  const serviceMenuItems: SideNavigationMenuItem[] = enableServiceMap
    ? [
        {
          icon: <TbCategory />,
          name: SERVICE_CONFIG_MENU.title,
          path: APP_PATH.CONFIG_SERVICES,
          childItems: buildServiceSidebarItems(services, selectedService, setSelectedService),
          leftSectionTitle: 'Service',
          leftChildItems: serviceGroupItems,
          rightSectionTitle: 'Select Service',
          headerAction: (close: () => void) => (
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
          ),
        },
      ]
    : [];

  return {
    serviceMenuItems,
  };
};
