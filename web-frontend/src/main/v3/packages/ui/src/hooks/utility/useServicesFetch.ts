import React from 'react';
import { useAtomValue, useSetAtom } from 'jotai';
import { configurationAtom, servicesAtom } from '@pinpoint-fe/ui/src/atoms';
import { useGetServices } from '@pinpoint-fe/ui/src/hooks';

export const useServicesFetch = () => {
  // configurationAtom(전역 store)을 기준으로 enable한다. fetch 인터셉터가 동일한
  // atom으로 enableServiceMap을 판단하므로, atom이 채워진 뒤에 /api/v2/services가
  // 발생해야 pServiceName 헤더가 누락되지 않는다. (raw config로 enable하면 atom이
  // useEffect로 채워지기 전에 요청이 나가 헤더가 빠진다.)
  const configuration = useAtomValue(configurationAtom);
  const enableServiceMap = !!configuration?.['experimental.enableServiceMap.value'];
  const { data: services } = useGetServices({ enabled: enableServiceMap });
  const setServices = useSetAtom(servicesAtom);

  React.useEffect(() => {
    if (!enableServiceMap) {
      setServices(undefined);
      return;
    }
    setServices(services);
  }, [enableServiceMap, services, setServices]);
};
