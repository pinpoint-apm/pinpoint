import React from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useExperimentals, useGetConfiguration, useServicesFetch } from '@pinpoint-fe/ui/src/hooks';
import { useAtomValue, useSetAtom } from 'jotai';
import {
  configurationAtom,
  searchParametersAtom,
  selectedServiceAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, Configuration } from '@pinpoint-fe/ui/src/constants';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';

export const InitialFetchOutlet = () => {
  const navigate = useNavigate();
  const { data, error } = useGetConfiguration<Configuration>();
  const setConfiguration = useSetAtom(configurationAtom);
  const configuration = useAtomValue(configurationAtom);
  const selectedService = useAtomValue(selectedServiceAtom);
  const queryClient = useQueryClient();
  const prevSelectedServiceRef = React.useRef(selectedService);
  const { pathname, search } = useLocation();
  const application = getApplicationTypeAndName(pathname);
  const searchParameters = Object.fromEntries(new URLSearchParams(search));
  const setSearchParameters = useSetAtom(searchParametersAtom);

  useExperimentals(data);
  useServicesFetch();

  // selectedService가 바뀌면 fetch 인터셉터가 주입하는 pServiceName 헤더 값이 달라진다.
  // 하지만 queryKey에는 service가 포함되지 않아, 캐시된 쿼리는 새 헤더로 재요청되지 않는다.
  // service 변경 시 캐시를 무효화하여 활성 쿼리가 새 service로 다시 요청되도록 한다.
  // enableServiceMap이 꺼진 환경에서는 인터셉터가 헤더를 주입하지 않으므로 무효화도 불필요하다.
  const enableServiceMap = !!configuration?.['experimental.enableServiceMap.value'];

  React.useEffect(() => {
    if (prevSelectedServiceRef.current === selectedService) return;
    prevSelectedServiceRef.current = selectedService;
    if (!enableServiceMap) return;
    queryClient.invalidateQueries();
  }, [selectedService, enableServiceMap, queryClient]);

  React.useEffect(() => {
    if (application && searchParameters) {
      setSearchParameters({ application, searchParameters });
    }
  }, [
    application?.applicationName,
    application?.serviceType,
    searchParameters?.to,
    searchParameters?.from,
  ]);

  React.useEffect(() => {
    setConfiguration(data);
  }, [data]);

  React.useEffect(() => {
    if (error) {
      navigate(APP_PATH.API_CHECK);
    }
  }, [error, navigate]);

  if (error) {
    return null;
  }

  if (!data || !configuration) {
    return null;
  }

  return <Outlet />;
};
