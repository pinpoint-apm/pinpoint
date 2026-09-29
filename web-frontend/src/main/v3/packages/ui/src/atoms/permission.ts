import { atom } from 'jotai';
import { getCurrentRouterPath } from '@pinpoint-fe/ui/src/utils/helper/route';

/**
 * 403을 받은 경로. 그 경로를 보고 있는 동안에만 권한 없음 화면으로 바뀐다. (이슈 #10744)
 *
 * 경로를 **함께** 담는 이유는 `serverMapCurrentTargetAtom`과 같다 — 아톰은 화면이 바뀌어도
 * 지워지지 않으므로, 판정을 effect로 되돌리면 한 박자 늦어 다음 화면이 한 번 권한 없음으로
 * 그려진다. 값에 경로를 찍어 두고 지금 경로와 비교하면 그 한 렌더도 남의 것으로 판정된다.
 *
 * 경로는 도장과 비교가 **같은 출처**(`getCurrentRouterPath`)를 써야 한다. 한쪽만
 * `useLocation()`을 읽으면 basename이 붙고 안 붙는 차이로 늘 어긋난다.
 *
 * **이 값을 읽는 화면만 덮인다**(`useIsForbiddenPath`). 도장은 403이 오면 어느 화면에서든 찍히고,
 * map 계열(serverMap·serviceMap·filteredMap과 그 realtime)은 **일부러 읽지 않는다.** map API에는
 * 권한 검사가 없어(`MapController`) 권한 없는 노드도 `isAuthorized: false`로 200에 실려 오고,
 * 403은 언제나 고른 노드를 묻는 조회에서 난다. 그것으로 화면을 덮으면 map이 사라져 다른 노드를
 * 고를 방법이 없어진다.
 */
export const forbiddenPathAtom = atom<string | undefined>(undefined);

/**
 * 지금 경로를 권한 없음으로 기록한다. 전역 쿼리 에러 핸들러(`reactQueryHelper`)가 렌더 밖에서
 * 기본 store를 통해 쓴다.
 */
export const markCurrentPathForbiddenAtom = atom(null, (_get, set) => {
  set(forbiddenPathAtom, getCurrentRouterPath());
});
