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
 */
export const forbiddenPathAtom = atom<string | undefined>(undefined);

/**
 * 권한 없음 안내(`Forbidden403`)를 그릴 수 있는 화면이 지금 몇 개 마운트되어 있는지.
 * `useIsForbiddenPath`를 부르는 화면이 마운트될 때 늘고 언마운트될 때 준다.
 *
 * 전역 쿼리 에러 핸들러는 이 값이 0이면 화면을 덮지 않고, 보고 있는 조회를 캐시에서 지우지도
 * 않는다. 안내를 그리지 않는 화면에서 지우면 에러를 그린 채 남은 컴포넌트가 다음 렌더에 새
 * 쿼리를 만들어 곧바로 다시 묻고, 그것이 또 403이 되어 요청이 끝없이 반복된다.
 * 경로 표(`coversPageOnForbidden`)만으로 가르면 표와 실제 화면이 어긋날 때 조용히 그 반복에
 * 빠지므로, "안내를 그리는가"는 화면이 직접 알린다.
 */
export const forbiddenNoticeMountCountAtom = atom(0);

/**
 * 지금 경로를 권한 없음으로 기록한다. 전역 쿼리 에러 핸들러(`reactQueryHelper`)가 렌더 밖에서
 * 기본 store를 통해 쓴다.
 */
export const markCurrentPathForbiddenAtom = atom(null, (_get, set) => {
  set(forbiddenPathAtom, getCurrentRouterPath());
});

/**
 * 조회 대상이 **경로를 바꾸지 않고** 바뀐 횟수. (이슈 #10744)
 *
 * 403이 지금 화면의 것인지는 "요청을 시작한 경로가 지금 경로인가"로도 가른다
 * (`handleGlobalQueryError`). 그런데 화면 안에서 대상을 고르는 곳(`Config > Agent Management`)은
 * 대상을 바꿔도 경로가 그대로라, A를 고른 직후 B로 바꾸면 늦게 도착한 A의 403이 B 화면을 덮었다.
 * 요청을 시작할 때 이 값도 함께 적어 두고, 값이 달라졌으면 이미 떠난 대상의 답으로 본다.
 *
 * 경로가 바뀌는 이동은 경로 비교로 이미 갈리므로 여기서 세지 않는다.
 */
export const forbiddenTargetGenerationAtom = atom(0);

/**
 * 화면 안에서 조회 대상을 바꿀 때 부른다. 앞서 받은 판정을 버리고, 그 전에 시작한 요청의 403이
 * 새 대상을 덮지 않도록 세대를 올린다. **경로를 바꾸지 않고 대상을 고르는 화면은 반드시 이것을
 * 부른다** — 빠뜨리면 이전 대상의 판정이 남거나 늦은 403이 새 대상을 덮는다.
 */
export const resetForbiddenTargetAtom = atom(null, (get, set) => {
  set(forbiddenPathAtom, undefined);
  set(forbiddenTargetGenerationAtom, get(forbiddenTargetGenerationAtom) + 1);
});
