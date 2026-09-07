import { useLocation } from 'react-router';
import { setRenderedRouterPath } from '@pinpoint-fe/ui/src/utils';

/**
 * 렌더 밖에서 읽는 "지금 경로"(`getCurrentRouterPath`)를 라우터가 렌더한 경로로 맞춘다.
 *
 * 요청 헤더(`resolveRequestService`)와 캐시 키(`serviceScopedQueryKeyHashFn`), map에서 고른
 * 선택의 경로 도장(`serverMapCurrentTargetAtom`)은 렌더 밖에서 경로를 읽어야 한다. 그것을
 * `window.location`에서 읽으면 뒤로/앞으로 가기에서 **라우터보다 앞서간다** — 브라우저가 주소를
 * 먼저 바꾸고 라우터의 location 상태는 그 다음 렌더에 반영되기 때문이다. 그 사이 한 렌더 동안
 * 조회 파라미터는 이전 경로의 것인데 service만 새 경로의 것이 되어, 캐시에 없는 키가 만들어지고
 * 곧바로 요청이 나간다. (이슈 #10587)
 *
 * **effect가 아니라 렌더 중에 갱신한다.** 같은 렌더 패스에서 아래쪽 조회 훅들이 이미 이 값을
 * 읽기 때문에, effect로 미루면 그 패스는 여전히 옛 값을 본다. 렌더한 location에서 그대로 파생된
 * 값이라 몇 번을 다시 렌더해도 같은 값이 된다.
 *
 * **조회를 하는 화면들보다 위에서 한 번 호출해야 한다**(`InitialFetchOutlet`). 아무도 호출하지
 * 않으면 `getCurrentRouterPath`가 `window.location` 폴백으로 동작하므로, 호출을 빠뜨린 저장소는
 * 예전과 똑같이 동작한다(고쳐지지 않을 뿐 깨지지는 않는다).
 */
export const useSyncRenderedRouterPath = () => {
  const { pathname } = useLocation();

  setRenderedRouterPath(pathname);

  return pathname;
};
