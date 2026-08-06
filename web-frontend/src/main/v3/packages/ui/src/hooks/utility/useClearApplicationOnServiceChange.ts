import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAtomValue, useSetAtom } from 'jotai';
import {
  currentServerAtom,
  searchParametersAtom,
  selectedServiceAtom,
  serverMapCurrentTargetAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { getServiceMapPath, getServiceNameFromPath } from '@pinpoint-fe/ui/src/utils';

/**
 * selectedService(선택된 서비스)가 바뀌면 이전 서비스에서 고른 application 선택을 무효화하고
 * 새 서비스의 servicemap으로 이동한다. 서비스마다 application 목록이 다르므로 이전 선택을
 * 유지하면 안 되고, 서비스를 바꿨다는 것은 그 서비스를 보겠다는 뜻이므로 시작 화면으로 보낸다.
 *
 * 1) searchParametersAtom의 application을 비운다. 사이드바 네비게이션(useMenuItems)이
 *    이 아톰의 application으로 각 페이지 링크를 만들기 때문에, 비우지 않으면 config 등
 *    다른 페이지에서 servermap으로 돌아갈 때 이전 application이 그대로 복원된다.
 * 2) map에서 고른 target(node/link)과 그 안에서 고른 서버도 비운다. 이 값들은 이전 service의
 *    map에 있던 노드를 가리키므로 새 service에서는 존재하지 않는다. 남겨두면 ChartsBoard가
 *    없는 노드를 기준으로 조회를 시작하는데, 새 경로에는 기준 application도 없어서
 *    applicationName 없는 요청이 나가 400을 받는다. (아톰은 전역이라 화면 remount로도 안 지워진다.)
 * 3) 새 service의 servicemap(`/serviceMap/{serviceName}`)으로 soft navigate 한다.
 *    serviceName을 경로에 실어야 그 화면의 모든 조회가 URL 기준으로 해석된다(진실의 원천).
 *    react-router `navigate`는 basename을 자동으로 붙이므로 경로를 그대로 넘기면 된다.
 *    (하드 리다이렉트가 아니므로 React Query 캐시와 ETag 캐시가 유지된다.)
 *    query string(?from=...&to=...)은 굳이 유지하지 않는다. 라우트 로더가 기본 시간 범위를
 *    채워주고, DEFAULT service는 어차피 application을 고르는 순간 다시 채워진다.
 *
 * 단, 경로가 이미 새 service를 가리키고 있으면(주소창을 직접 고쳐 들어온 경우 —
 * `useSyncSelectedServiceWithPath`가 그 값을 전역 선택값에 반영한다) 2)까지만 하고 멈춘다.
 * 사용자가 보려고 지정한 화면이므로 servicemap으로 끌고 가면 안 되고, application도 경로에서
 * 다시 동기화되므로(InitialFetchOutlet) 비우면 오히려 어긋난다.
 *
 * enabled(enableServiceMap)가 아니면 아무 것도 하지 않는다. servicemap 자체가
 * 그 설정이 켜져 있을 때만 존재하는 화면이다.
 */
export const useClearApplicationOnServiceChange = (enabled: boolean) => {
  const selectedService = useAtomValue(selectedServiceAtom);
  const setSearchParameters = useSetAtom(searchParametersAtom);
  const setServerMapCurrentTarget = useSetAtom(serverMapCurrentTargetAtom);
  const setCurrentServer = useSetAtom(currentServerAtom);
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const prevSelectedServiceRef = React.useRef(selectedService);

  React.useEffect(() => {
    if (prevSelectedServiceRef.current === selectedService) return;
    prevSelectedServiceRef.current = selectedService;
    if (!enabled) return;

    // 이전 service의 map에서 고른 선택 무효화. 어느 경로로 바뀌었든 무효다.
    setServerMapCurrentTarget(undefined);
    setCurrentServer(undefined);

    // 경로가 이미 새 service를 가리키면 사용자가 주소창에서 바꿔 들어온 것이다. 화면을 옮기지 않는다.
    if (getServiceNameFromPath(pathname) === selectedService) {
      return;
    }

    // 저장된 application 무효화 → 네비게이션 링크가 base 경로로 바뀐다.
    setSearchParameters((prev) => ({ ...prev, application: {} as ApplicationType }));

    navigate(getServiceMapPath(selectedService), { replace: true });
  }, [
    selectedService,
    enabled,
    pathname,
    setSearchParameters,
    setServerMapCurrentTarget,
    setCurrentServer,
    navigate,
  ]);
};
