import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import {
  getApplicationTypeAndName,
  getRealtimePath,
  getServerMapPath,
  getServiceMapPath,
  getServiceMapRealtimePath,
  parseServiceScopedPath,
} from '@pinpoint-fe/ui/src/utils';

/** 라우터 기준 pathname(basename 제외)이 해당 페이지의 경로인지 여부. */
const isUnderPage = (pathname: string, pagePath: string) =>
  pathname === pagePath || pathname.startsWith(`${pagePath}/`);

export interface HiddenMapPageRedirectParams {
  /**
   * 라우터 기준 pathname(basename 제외). **인코딩된 raw 값**이어야 한다.
   * 디코딩된 값을 넘기면 serviceName 안의 '%2F'가 '/'로 풀려 세그먼트 경계가 어긋난다.
   */
  pathname: string;
  /** '?'를 포함한 query string. 없으면 빈 문자열. */
  search: string;
  enableServiceMap: boolean;
  /** servermap → servicemap으로 옮길 때 경로에 실을 service. 반대 방향에서는 쓰지 않는다. */
  serviceName: string;
}

/**
 * `enableServiceMap` 설정에 따라 사이드 메뉴에서 감춘 map 화면의 URL을, 남아 있는 쪽의 같은
 * 화면으로 옮길 목적지를 정한다. 옮길 필요가 없으면 undefined다.
 *
 * | enableServiceMap | 보이는 메뉴 | 감춰서 옮기는 경로 |
 * |---|---|---|
 * | 켜짐 | servicemap | `/serverMap...` → `/serviceMap...` |
 * | 꺼짐 | servermap | `/serviceMap...` → `/serverMap...` |
 *
 * **메뉴에서 감추는 것만으로는 부족하다.** 링크·북마크·뒤로가기로 감춘 쪽 URL에 그대로 들어올 수
 * 있고, 그러면 메뉴에는 없는 화면이 그려진다. 첫 진입 경로(`/` → `/serverMap`)도 여기로 걸린다.
 *
 * **여기는 경로 매핑만 하는 순수 함수다.** 설정을 어디서 읽는지는 부르는 쪽이 정한다 —
 * 이 프로젝트의 두 갈래(`getEnableServiceMap` / `useEnableServiceMap`)를 그대로 따르되,
 * "어디로 옮기는가"는 두 갈래가 반드시 같아야 하므로 이 함수 하나로 모은다.
 *
 * | 부르는 곳 | 언제 | 설정을 읽는 길 |
 * |---|---|---|
 * | `resolveHiddenMapPageRedirect` (라우트 로더) | 화면에 들어올 때 | `getConfiguration` + localStorage |
 * | `useHiddenMapPageRedirect` (`InitialFetchOutlet`) | 이미 열린 화면에서 설정이 바뀔 때 | `useEnableServiceMap` |
 *
 * 두 방향이 서로 반대 조건이라 리다이렉트가 되돌아오지 않는다 — 목적지에서 다시 물어보면
 * 같은 설정을 보고 "옮길 필요 없음"으로 판단한다.
 */
export const getHiddenMapPageRedirect = ({
  pathname,
  search,
  enableServiceMap,
  serviceName,
}: HiddenMapPageRedirectParams): string | undefined => {
  // 세그먼트가 더 긴 실시간 보기를 먼저 본다. `/serverMap/realtime`은 `/serverMap`의 하위
  // 경로라, 순서가 뒤바뀌면 'realtime'을 application 세그먼트로 읽는다.
  if (enableServiceMap) {
    const isServerMapRealtime = isUnderPage(pathname, APP_PATH.SERVER_MAP_REALTIME);

    if (!isServerMapRealtime && !isUnderPage(pathname, APP_PATH.SERVER_MAP)) {
      return undefined;
    }

    // DEFAULT가 아닌 service는 소속 application을 모두 모아 그려 기준 application이 없다.
    // 그대로 실어 보내면 목적지 로더가 곧 지우면서 한 번 더 움직인다.
    const application =
      serviceName === DEFAULT_SERVICE ? getApplicationTypeAndName(pathname) : null;

    // 실시간 보기는 기간을 화면이 직접 만들기 때문에 query string을 싣지 않는다.
    return isServerMapRealtime
      ? getServiceMapRealtimePath(serviceName, application)
      : `${getServiceMapPath(serviceName, application)}${search}`;
  }

  if (isUnderPage(pathname, APP_PATH.SERVICE_MAP_REALTIME)) {
    const { application } = parseServiceScopedPath(APP_PATH.SERVICE_MAP_REALTIME, pathname);

    return getRealtimePath(application);
  }

  if (isUnderPage(pathname, APP_PATH.SERVICE_MAP)) {
    const { application } = parseServiceScopedPath(APP_PATH.SERVICE_MAP, pathname);

    // 기간(from/to)은 목적지에도 싣는다. servermap도 같은 기간을 보여줘야 하는데, 떨어뜨리면
    // 로더가 기본 기간으로 채워 사용자가 보던 기간이 초기화된다.
    return `${getServerMapPath(application)}${search}`;
  }

  return undefined;
};
