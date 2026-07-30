import React from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  resolveRequestService,
  useClearApplicationOnServiceChange,
  useExperimentals,
  useGetConfiguration,
  useServicesFetch,
} from '@pinpoint-fe/ui/src/hooks';
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
  const { pathname, search } = useLocation();
  const application = getApplicationTypeAndName(pathname);
  const searchParameters = Object.fromEntries(new URLSearchParams(search));
  const setSearchParameters = useSetAtom(searchParametersAtom);

  const enableServiceMap = !!configuration?.['experimental.enableServiceMap.value'];
  // 쿼리 캐시는 "요청이 해석되는 service"(serviceScopedQueryKeyHashFn)로 분리되지만,
  // 그 해시는 store를 명령형으로 읽으므로 쿼리 훅이 다시 렌더링되지 않으면 갱신되지 않는다.
  // service를 바꿨는데 해시가 그대로면 새 헤더로 받은 응답이 이전 service 키에 쌓인다.
  // 이 값을 key로 두어 페이지 서브트리를 remount 해, 모든 쿼리가 새 service 키로 다시 붙게 한다.
  // (사이드 네비게이션은 상위 SideNavigationOutlet에 있어 remount 대상이 아니다.)
  // 해시와 동일한 값을 얻으려면 경로 판단도 해시와 같은 함수(window.location 기준)를 써야 한다.
  const selectedService = useAtomValue(selectedServiceAtom);
  const requestService = resolveRequestService(selectedService);

  useExperimentals(data);
  useServicesFetch(configuration);
  useClearApplicationOnServiceChange(enableServiceMap);

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

  return (
    <React.Fragment key={requestService}>
      <Outlet />
    </React.Fragment>
  );
};
