import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAtomValue, useSetAtom } from 'jotai';
import { searchParametersAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';

/**
 * 주어진 pathname에서 application 세그먼트를 떼어낸 base 경로를 반환한다.
 * pathname에 application이 없으면 null을 반환한다(리다이렉트 불필요).
 *
 * application 세그먼트는 항상 경로의 마지막 세그먼트다. getApplicationTypeAndName으로
 * 존재를 확인한 뒤 마지막 세그먼트만 떼어내므로, 세그먼트 형식 정규식을 여기서
 * 중복 정의하지 않는다(파싱 규칙이 바뀌어도 어긋날 일이 없다).
 * 마지막 세그먼트만 제거하므로, 넘긴 pathname에 접두사(basename 등)가 있으면 그대로 유지된다.
 */
export const resolveClearedApplicationPath = (pathname: string): string | null => {
  if (!getApplicationTypeAndName(pathname)) return null;
  return pathname.split('/').slice(0, -1).join('/') || '/';
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
    // query string(?from=...&to=...)은 굳이 유지하지 않는다. application이 비워진
    // 중간 상태에서는 지도가 렌더링되지 않고, 사용자가 새 application을 고르는 순간
    // from/to 없는 경로로 이동해 어차피 기본 시간 범위로 리셋되기 때문이다.
    const target = resolveClearedApplicationPath(pathname);
    if (target) navigate(target, { replace: true });
  }, [selectedService, enabled, setSearchParameters, navigate, pathname]);
};
