import { useLocation } from 'react-router';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import { getHiddenMapPageRedirect } from '@pinpoint-fe/ui/src/loader/hiddenMapPageRedirect';
import { useEnableServiceMap } from './useEnableServiceMap';
import { useServiceNameForLink } from './useServiceNameForLink';

/**
 * 이미 열려 있는 화면이 **지금** 감춰진 map 화면인지 보고, 그렇다면 옮길 목적지를 반환한다.
 * 옮길 필요가 없으면 undefined다.
 *
 * 라우트 로더(`resolveHiddenMapPageRedirect`)는 화면에 **들어올 때** 한 번만 판단한다. 그래서
 * 다른 탭에서 Experimental 설정을 바꾸면 이미 열려 있던 탭은 그대로 남았다 — 그런데 메뉴와
 * 요청 헤더는 매 렌더/매 요청마다 값을 다시 읽으므로 **그 둘만 갈아타** 어긋난 화면이 된다.
 * (servicemap 화면인데 `pServiceName` 헤더가 빠져 나가면 백엔드가 기본 service로 해석하므로,
 * 화면은 멀쩡히 그려지고 숫자만 엉뚱하게 온다.) 그 탭도 같이 옮기기 위한 훅이다.
 *
 * 설정은 화면용 갈래(`useEnableServiceMap`)로 읽고, 어디로 옮기는지는 로더와 같은 함수
 * (`getHiddenMapPageRedirect`)가 정한다. 두 갈래가 갈리면 로더는 옮기는데 화면은 안 옮기거나
 * (또는 그 반대로) 서로 되돌리는 왕복이 생긴다.
 *
 * **호출부는 effect가 아니라 렌더에서 이 값을 쓴다.** effect로 옮기면 한 박자 늦어, 어긋난 상태로
 * 화면이 한 번 더 그려지며 그 사이 조회가 나간다. → `InitialFetchOutlet`
 */
export const useHiddenMapPageRedirect = () => {
  const enableServiceMap = useEnableServiceMap();
  // servermap 경로에는 serviceName이 실리지 않아 전역 선택값으로 폴백된다(로더의
  // `getRequestService`와 같은 규칙). 설정이 꺼져 있으면 undefined인데, 그 방향
  // (servicemap → servermap)에서는 serviceName을 쓰지 않으므로 값이 무엇이든 결과가 같다.
  const serviceName = useServiceNameForLink() ?? DEFAULT_SERVICE;
  // react-router의 `location`은 basename을 뗀 raw pathname을 준다(`params`와 달리 디코딩되지
  // 않는다). `<Navigate>`가 basename을 다시 붙이므로 목적지도 같은 기준으로 만들어진다.
  const { pathname, search } = useLocation();

  return getHiddenMapPageRedirect({ pathname, search, enableServiceMap, serviceName });
};
