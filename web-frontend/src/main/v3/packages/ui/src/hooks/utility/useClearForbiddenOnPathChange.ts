import React from 'react';
import { useLocation } from 'react-router';
import { useAtom } from 'jotai';
import { forbiddenPathAtom } from '@pinpoint-fe/ui/src/atoms';
import { getCurrentRouterPath } from '@pinpoint-fe/ui/src/utils';

/**
 * 권한 없음 판정을 **그 화면을 떠날 때** 버린다. (이슈 #10744)
 *
 * 판정이 서면 페이지가 본문을 언마운트하므로 그 화면의 조회는 멈춘다. 즉 그 경로에서는 판정을
 * 되돌릴 성공 응답이 두 번 다시 오지 않는다(`handleGlobalQuerySuccess`). 도장을 남겨 두면
 * 같은 URL로 다시 들어왔을 때 **재조회 없이** 곧바로 권한 없음 화면이 떠서, 그 사이 권한이
 * 생겼어도 새로고침 전까지 알 수 없다.
 *
 * 그래서 경로가 바뀌는 순간 버린다. 다시 들어오면 본문이 그려지고 조회가 나가, 여전히 막혀
 * 있으면 다시 판정이 서고 열렸으면 화면이 보인다 — 새로고침과 같은 결과가 된다.
 *
 * **조회 화면들보다 위에서 한 번 호출한다**(`InitialFetchOutlet`). 판정이 선 화면에서 떠나면
 * 그 페이지는 언마운트되므로, 페이지 안에서 지우면 지울 기회 자체가 없다.
 *
 * effect로 지워도 늦지 않다. 지금 경로와 도장을 비교하는 쪽(`useIsForbiddenPath`)이 경로가
 * 바뀐 그 렌더부터 이미 false를 주므로, 이 effect는 남은 값을 정리하는 일만 한다.
 */
export const useClearForbiddenOnPathChange = () => {
  const { pathname } = useLocation();
  const [forbiddenPath, setForbiddenPath] = useAtom(forbiddenPathAtom);

  React.useEffect(() => {
    // 비교는 도장을 찍을 때와 같은 출처로 한다. `useLocation()`은 경로가 바뀔 때 이 effect를
    // 다시 돌리기 위한 구독 용도다.
    if (forbiddenPath !== undefined && forbiddenPath !== getCurrentRouterPath()) {
      setForbiddenPath(undefined);
    }
  }, [pathname, forbiddenPath, setForbiddenPath]);
};
