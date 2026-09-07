import { useLocation } from 'react-router';
import { useAtomValue } from 'jotai';
import {
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
  serverMapCurrentTargetPathAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { getCurrentRouterPath } from '@pinpoint-fe/ui/src/utils';

/**
 * 아톰에 담긴 선택이 **지금 보고 있는 경로에서 고른 것인지** 여부.
 *
 * 아톰은 경로가 바뀌어도 그대로 남아 있고, 페이지들은 경로 변경 effect에서 비운다. effect는 한
 * 박자 늦으므로 그 사이 한 렌더 동안 이전 경로의 선택이 보인다. 선택에 찍어 둔 경로와 지금
 * 경로를 비교하면 그 한 렌더도 남의 것으로 판정할 수 있다.
 *
 * 지금 경로는 아톰이 도장 찍을 때와 **같은 출처**(`getCurrentRouterPath`)에서 읽는다. 한쪽만
 * `useLocation()`의 값을 쓰면 basename이나 라우터 종류에 따라 두 값이 어긋나 선택이 늘 남의
 * 것으로 판정될 수 있다. `useLocation()`은 경로가 바뀔 때 다시 렌더시키는 구독 용도로만 쓴다.
 *
 * 선택이 아예 없으면(도장도 없으면) "지금 경로의 것"으로 본다 — 없는 것은 어느 경로에서도
 * 똑같이 없기 때문이다.
 */
const useIsTargetOnCurrentPath = () => {
  useLocation();
  const targetPath = useAtomValue(serverMapCurrentTargetPathAtom);

  return targetPath === undefined || targetPath === getCurrentRouterPath();
};

/**
 * 지금 경로에서 고른 map의 노드/링크. 이전 경로에서 고른 것은 undefined다.
 *
 * 조회 파라미터를 만드는 곳은 아톰(`serverMapCurrentTargetAtom`)을 직접 읽지 말고 이 훅을 쓴다.
 * 직접 읽으면 경로가 바뀐 직후 한 렌더 동안 **(새 application, 이전 경로의 노드)** 짝으로
 * 조회가 한 번 더 나간다. (이슈 #10587)
 */
export const useServerMapCurrentTarget = () => {
  const currentTarget = useAtomValue(serverMapCurrentTargetAtom);

  return useIsTargetOnCurrentPath() ? currentTarget : undefined;
};

/** 위와 같은 규칙으로 읽는 `serverMapCurrentTargetDataAtom`. */
export const useServerMapCurrentTargetData = () => {
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom);

  return useIsTargetOnCurrentPath() ? currentTargetData : undefined;
};
