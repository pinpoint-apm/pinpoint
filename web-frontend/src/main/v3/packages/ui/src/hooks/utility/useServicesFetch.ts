import React from 'react';
import { useSetAtom } from 'jotai';
import { servicesAtom } from '@pinpoint-fe/ui/src/atoms';
import { useGetServices } from '@pinpoint-fe/ui/src/hooks';
import { useEnableServiceMap } from './useEnableServiceMap';

export const useServicesFetch = () => {
  // configurationAtom에서 직접 읽어 enable을 판단한다(`useEnableServiceMap`). fetch 인터셉터도
  // 동일한 atom과 동일한 규칙으로 enableServiceMap을 판단하므로, atom이 채워진 뒤에
  // /api/v2/services가 발생해야 pServiceName 헤더가 누락되지 않는다. (raw config로 enable하면
  // atom이 채워지기 전에 요청이 나가 헤더가 빠진다. atom을 직접 읽는 지금 구조가 그 순서를 보장한다.)
  const enableServiceMap = useEnableServiceMap();
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
