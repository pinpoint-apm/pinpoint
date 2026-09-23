import React from 'react';
import { useLocation } from 'react-router';
import { useAtomValue, useSetAtom } from 'jotai';
import { forbiddenNoticeMountCountAtom, forbiddenPathAtom } from '@pinpoint-fe/ui/src/atoms';
import { getCurrentRouterPath } from '@pinpoint-fe/ui/src/utils';

/**
 * 지금 보고 있는 경로가 403을 받은 경로인지. (이슈 #10744)
 *
 * 도장을 찍을 때와 **같은 출처**(`getCurrentRouterPath`)로 비교한다. 한쪽만 `useLocation()`의
 * 값을 쓰면 basename이 붙고 안 붙는 차이로 늘 어긋난다 — `useServerMapCurrentTarget`과 같은
 * 이유다. `useLocation()`은 경로가 바뀔 때 다시 렌더시키는 구독 용도로만 쓴다.
 *
 * **호출부는 effect가 아니라 렌더에서 이 값을 쓴다.** effect로 화면을 바꾸면 한 박자 늦어,
 * 권한 없는 화면이 한 번 더 그려지며 그 사이 남은 조회들이 또 나간다.
 *
 * **이 훅을 부르는 것은 "이 화면은 권한 없음 안내를 그린다"는 선언이다.** 마운트되어 있는 동안
 * `forbiddenNoticeMountCountAtom`을 올려 두고, 전역 에러 핸들러는 그 값이 있을 때만 화면을
 * 덮고 조회를 캐시에서 지운다. 그러니 값을 받아 `Forbidden403`을 그리지 않을 곳에서는 부르지 않는다.
 */
export const useIsForbiddenPath = () => {
  useLocation();
  const forbiddenPath = useAtomValue(forbiddenPathAtom);
  const setNoticeMountCount = useSetAtom(forbiddenNoticeMountCountAtom);

  React.useEffect(() => {
    setNoticeMountCount((count) => count + 1);
    return () => setNoticeMountCount((count) => count - 1);
  }, [setNoticeMountCount]);

  return forbiddenPath !== undefined && forbiddenPath === getCurrentRouterPath();
};
