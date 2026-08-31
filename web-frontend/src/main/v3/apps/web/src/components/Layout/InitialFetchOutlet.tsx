import React from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router';
import {
  resolveRequestService,
  useClearApplicationOnServiceChange,
  useExperimentals,
  useGetConfiguration,
  useServicesFetch,
  useSyncSelectedServiceWithPath,
} from '@pinpoint-fe/ui/src/hooks';
import { useAtomValue, useSetAtom } from 'jotai';
import {
  configurationAtom,
  searchParametersAtom,
  selectedServiceAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, Configuration } from '@pinpoint-fe/ui/src/constants';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';
import { NotFound404 } from '@pinpoint-fe/ui';

export const InitialFetchOutlet = () => {
  const navigate = useNavigate();
  const { data, error } = useGetConfiguration<Configuration>();
  const setConfiguration = useSetAtom(configurationAtom);
  const configuration = useAtomValue(configurationAtom);
  const { pathname, search } = useLocation();
  // serviceName이 실린 경로에서도 application 세그먼트는 그대로 마지막에 온다.
  // (사이드 네비게이션이 이 값으로 다른 페이지 링크를 만든다.)
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
  useServicesFetch();
  // 경로에 실린 serviceName을 전역 선택값에 반영한 뒤(URL이 진실의 원천),
  // 그 변경에 따라 이전 service에서 고른 값들을 무효화한다. 순서가 이 방향이어야 한다.
  const { isUnknownServiceInPath } = useSyncSelectedServiceWithPath(enableServiceMap);
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

  // 없는 service를 가리키는 경로다. 그 이름으로는 어떤 조회도 의미가 없으므로 화면을 그리지 않는다.
  // 다른 service로 바꿔 보여주면 사용자는 자기가 요청한 것과 다른 것을 보고 있는 줄 모른다.
  if (isUnknownServiceInPath) {
    return <NotFound404 />;
  }

  return (
    <React.Fragment key={requestService}>
      <Outlet />
    </React.Fragment>
  );
};
