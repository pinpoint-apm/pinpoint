import React from 'react';
import { useSetAtom } from 'jotai';
import { servicesAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';
import { useGetServices } from '@pinpoint-fe/ui/src/hooks';

export const useServicesFetch = (configuration: Configuration | undefined) => {
  // configurationAtom(전역 store)에서 읽은 값을 그대로 받아 enable한다. fetch 인터셉터가
  // 동일한 atom으로 enableServiceMap을 판단하므로, atom이 채워진 뒤에 /api/v2/services가
  // 발생해야 pServiceName 헤더가 누락되지 않는다. (raw config로 enable하면 atom이
  // useEffect로 채워지기 전에 요청이 나가 헤더가 빠지므로, 호출자는 raw config가 아니라
  // configurationAtom 값을 넘겨야 한다.)
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
