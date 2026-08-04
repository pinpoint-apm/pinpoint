import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAtomValue, useSetAtom } from 'jotai';
import { searchParametersAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, ApplicationType } from '@pinpoint-fe/ui/src/constants';

/**
 * selectedService(선택된 서비스)가 바뀌면 이전 서비스에서 고른 application 선택을 무효화하고
 * 새 서비스의 servicemap으로 이동한다. 서비스마다 application 목록이 다르므로 이전 선택을
 * 유지하면 안 되고, 서비스를 바꿨다는 것은 그 서비스를 보겠다는 뜻이므로 시작 화면으로 보낸다.
 *
 * 1) searchParametersAtom의 application을 비운다. 사이드바 네비게이션(useMenuItems)이
 *    이 아톰의 application으로 각 페이지 링크를 만들기 때문에, 비우지 않으면 config 등
 *    다른 페이지에서 servermap으로 돌아갈 때 이전 application이 그대로 복원된다.
 * 2) application 세그먼트가 없는 `/serviceMap`으로 soft navigate 한다. react-router
 *    `navigate`는 basename을 자동으로 붙이므로 APP_PATH를 그대로 넘기면 된다.
 *    (하드 리다이렉트가 아니므로 React Query 캐시와 ETag 캐시가 유지된다.)
 *    query string(?from=...&to=...)은 굳이 유지하지 않는다. application이 없는 경로에서는
 *    지도가 렌더링되지 않고, 사용자가 새 application을 고르는 순간 라우트 로더가
 *    기본 시간 범위를 채워주기 때문이다.
 *
 * enabled(enableServiceMap)가 아니면 아무 것도 하지 않는다. servicemap 자체가
 * 그 설정이 켜져 있을 때만 존재하는 화면이다.
 */
export const useClearApplicationOnServiceChange = (enabled: boolean) => {
  const selectedService = useAtomValue(selectedServiceAtom);
  const setSearchParameters = useSetAtom(searchParametersAtom);
  const navigate = useNavigate();
  const prevSelectedServiceRef = React.useRef(selectedService);

  React.useEffect(() => {
    if (prevSelectedServiceRef.current === selectedService) return;
    prevSelectedServiceRef.current = selectedService;
    if (!enabled) return;

    // 저장된 application 무효화 → 네비게이션 링크가 base 경로로 바뀐다.
    setSearchParameters((prev) => ({ ...prev, application: {} as ApplicationType }));

    navigate(APP_PATH.SERVICE_MAP, { replace: true });
  }, [selectedService, enabled, setSearchParameters, navigate]);
};
