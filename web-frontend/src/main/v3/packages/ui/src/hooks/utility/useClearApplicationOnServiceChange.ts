import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAtomValue, useSetAtom } from 'jotai';
import { searchParametersAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';

// URL 맨 끝의 application 세그먼트(`{appName}@{serviceType}` 또는 `^`)를 매칭한다.
// getApplicationTypeAndName과 동일한 규칙으로 base 경로를 계산한다.
const APPLICATION_PATH_SEGMENT = /\/?([^/]+)[@^]([^/]+)$/;

/**
 * 주어진 pathname에서 application 세그먼트를 떼어낸 base 경로를 반환한다.
 * pathname에 application이 없으면 null을 반환한다(리다이렉트 불필요).
 * 세그먼트만 제거하므로, 넘긴 pathname에 접두사(basename 등)가 있으면 그대로 유지된다.
 */
export const resolveClearedApplicationPath = (pathname: string): string | null => {
  if (!getApplicationTypeAndName(pathname)) return null;
  return pathname.replace(APPLICATION_PATH_SEGMENT, '') || '/';
};

/**
 * selectedService(선택된 서비스)가 바뀌면 이전 서비스에서 고른 application 선택을 무효화한다.
 * 서비스마다 application 목록이 다르므로 이전 선택을 유지하면 안 된다.
 *
 * 1) searchParametersAtom의 application을 비운다. 사이드바 네비게이션(useMenuItems)이
 *    이 아톰의 application으로 각 페이지 링크를 만들기 때문에, 비우지 않으면 config 등
 *    다른 페이지에서 servermap으로 돌아갈 때 이전 application이 그대로 복원된다.
 * 2) 현재 URL에 application이 있으면 그 세그먼트를 떼어낸 base 경로로 soft navigate 한다.
 *    react-router `navigate`는 basename을 자동으로 붙이므로, basename이 제거된
 *    라우터 pathname(useLocation)을 기준으로 경로를 계산한다.
 *    (하드 리다이렉트가 아니므로 React Query 캐시와 ETag 캐시가 유지된다.)
 *
 * enabled(enableServiceMap)가 아니면 아무 것도 하지 않는다.
 */
export const useClearApplicationOnServiceChange = (enabled: boolean) => {
  const selectedService = useAtomValue(selectedServiceAtom);
  const setSearchParameters = useSetAtom(searchParametersAtom);
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const prevSelectedServiceRef = React.useRef(selectedService);

  React.useEffect(() => {
    if (prevSelectedServiceRef.current === selectedService) return;
    prevSelectedServiceRef.current = selectedService;
    if (!enabled) return;

    // 저장된 application 무효화 → 네비게이션 링크가 base 경로로 바뀐다.
    setSearchParameters((prev) => ({ ...prev, application: {} as ApplicationType }));

    // 현재 URL에 application이 있으면 base 경로로 soft navigate.
    const target = resolveClearedApplicationPath(pathname);
    if (target) navigate(target, { replace: true });
  }, [selectedService, enabled, setSearchParameters, navigate, pathname]);
};
