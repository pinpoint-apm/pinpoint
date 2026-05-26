import React from 'react';
import { useSetAtom } from 'jotai';
import { servicesAtom } from '@pinpoint-fe/ui/src/atoms';
import { useGetServices } from '@pinpoint-fe/ui/src/hooks';
import type { Configuration } from '@pinpoint-fe/ui/src/constants';

export const useServicesFetch = (configuration: Configuration | undefined) => {
  const enableServiceMap = !!(
    configuration as (Configuration & Record<string, unknown>) | undefined
  )?.['experimental.enableServiceMap.value'];
  const { data: services } = useGetServices({ enabled: enableServiceMap });
  const setServices = useSetAtom(servicesAtom);

  React.useEffect(() => {
    setServices(services);
  }, [services]);
};
