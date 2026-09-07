import React from 'react';
import { Navigate, Outlet, useLocation, useNavigate } from 'react-router';
import {
  resolveRequestService,
  useClearApplicationOnServiceChange,
  useEnableServiceMap,
  useExperimentals,
  useGetConfiguration,
  useHiddenMapPageRedirect,
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

  // configuration 기본값 위에 사용자가 Experimental 설정에서 고른 값(localStorage)이 얹힌다.
  const enableServiceMap = useEnableServiceMap();
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
  // 지금 보고 있는 화면이 설정에 따라 감춰진 map 화면이 되었는지. 값이 있으면 아래에서 옮긴다.
  const hiddenMapPageRedirect = useHiddenMapPageRedirect();

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

  // 다른 탭에서 Experimental 설정을 바꿔, 보고 있던 화면이 메뉴에서 감춰진 map이 된 경우다.
  // 로더는 이 화면에 들어올 때 한 번만 판단하므로 여기서 다시 본다. 메뉴와 요청 헤더는 이미
  // 새 설정으로 갈아탔으니, 화면만 남겨두면 servicemap을 보면서 service 없이 조회하는(또는 그
  // 반대의) 어긋난 탭이 된다.
  //
  // **effect가 아니라 렌더에서 옮긴다.** effect로 두면 한 박자 늦어, 어긋난 상태로 자식이 한 번
  // 더 그려지며 그 사이 조회가 나간다. `replace`인 이유는 뒤로 가도 로더가 같은 판단으로 다시
  // 옮기기 때문이다 — history에 돌아갈 수 없는 항목만 쌓인다.
  if (hiddenMapPageRedirect) {
    return <Navigate to={hiddenMapPageRedirect} replace />;
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
