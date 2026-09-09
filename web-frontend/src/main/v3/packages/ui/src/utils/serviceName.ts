import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/constants';
import { getServiceNameFromPath } from './helper/application';

export interface PickServiceNameParams {
  enableServiceMap: boolean;
  /**
   * 라우터 기준 pathname(basename 제외). **인코딩된 raw 값**이어야 한다. 디코딩된 값을 넘기면
   * serviceName 안의 '%2F'가 '/'로 풀려 세그먼트 경계가 어긋난다.
   */
  pathname: string;
  /** 전역으로 선택한 service(`selectedServiceAtom`). */
  selectedService?: string;
}

/**
 * **어느 service로 조회하는지를 정하는 단 하나의 규칙.** 요청 헤더·캐시 키·화면·링크가 모두
 * 이 함수를 지난다. 갈래가 생기면 헤더는 A service로 나가는데 캐시는 B service 키에 쌓이거나,
 * 화면에는 service 개념이 없는데 헤더만 실려 나가는 어긋남이 생긴다.
 *
 * 순서는 이 하나뿐이다.
 *
 * 1. `enableServiceMap`이 꺼져 있으면 → **undefined.** service 개념 자체가 없다. 백엔드가 헤더
 *    없는 요청을 기본 service로 해석하므로 화면도 service가 없는 것처럼 동작해야 하고, 설정이
 *    꺼진 저장소로 헤더가 새어 나가서도 안 된다.
 * 2. 경로에 serviceName이 실려 있으면 → 그 값. URL이 진실의 원천이다. 전역 선택값은 탭 간
 *    공유 저장소라, 링크를 새 탭에 열어 둔 뒤 원래 탭에서 service를 바꾸면 화면과 어긋난다.
 * 3. 아직 serviceName을 싣지 않는 화면이면 → 전역 선택값.
 * 4. 그것도 없으면 → `DEFAULT`. 백엔드가 헤더 없는 요청을 해석하는 값과 같아서, 헤더를 실어도
 *    싣지 않은 것과 같은 조회가 된다.
 *
 * 두 갈래의 어댑터가 이 함수를 감싼다 — 다른 곳에서는 직접 부르지 않는다.
 *
 * | 부르는 곳 | 어댑터 |
 * |---|---|
 * | 화면(훅·컴포넌트) | `useRequestService()` |
 * | 렌더 밖(fetch 인터셉터, 캐시 키, 라우트 로더) | `getRequestService()` |
 */
export const pickServiceName = ({
  enableServiceMap,
  pathname,
  selectedService,
}: PickServiceNameParams): string | undefined => {
  if (!enableServiceMap) {
    return undefined;
  }

  return getServiceNameFromPath(pathname) || selectedService || DEFAULT_SERVICE;
};
