import { useLocation } from 'react-router';
import { useAtomValue } from 'jotai';
import { selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { pickServiceName } from '@pinpoint-fe/ui/src/utils';
import { useEnableServiceMap } from './useEnableServiceMap';

/**
 * 화면에서 읽는 "이 화면의 조회가 나갈 service". 규칙은 `pickServiceName` 하나뿐이다
 * (enableServiceMap → 경로의 serviceName → 전역 선택값 → DEFAULT).
 *
 * 조회 헤더, 다른 화면으로 넘기는 링크(`getTransactionListPath` 등), 사이드 메뉴, DEFAULT 여부
 * 판단이 모두 이 훅을 지난다. 렌더 밖(fetch 인터셉터·캐시 키·라우트 로더)의 갈래는
 * `getRequestService`이고 같은 규칙을 쓴다 — 갈리면 헤더는 A service로 나가는데 캐시는
 * B service 키에 쌓이거나, 화면에는 service 개념이 없는데 헤더만 실려 나간다.
 *
 * `enableServiceMap`이 꺼져 있으면 undefined다. service 개념 자체가 없으므로 링크에 세그먼트를
 * 싣지 않고(경로 형태가 예전과 같아진다) 헤더도 실리지 않는다.
 *
 * 켜져 있으면 **언제나 문자열**이다(마지막 폴백이 DEFAULT). 그래서 호출부가 `?? DEFAULT_SERVICE`를
 * 다시 붙일 필요가 없다.
 */
export const useRequestService = () => {
  const enableServiceMap = useEnableServiceMap();
  const selectedService = useAtomValue(selectedServiceAtom);
  const { pathname } = useLocation();

  return pickServiceName({ enableServiceMap, pathname, selectedService });
};
