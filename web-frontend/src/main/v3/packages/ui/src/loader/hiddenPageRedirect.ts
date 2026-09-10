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

/**
 * 끝에 붙은 '/'를 뗀다. 라우터는 `/config/auth`와 `/config/auth/`를 같은 화면으로 매칭하므로,
 * 경로를 그대로 비교하면 '/'가 붙은 링크·북마크로 감춘 화면에 그대로 들어올 수 있다.
 * 루트('/')는 그 자체가 경로라 남긴다.
 */
const withoutTrailingSlash = (pathname: string) =>
  pathname.length > 1 ? pathname.replace(/\/+$/, '') : pathname;

export interface HiddenPageRedirectParams {
  /**
   * 라우터 기준 pathname(basename 제외). **인코딩된 raw 값**이어야 한다.
   * 디코딩된 값을 넘기면 serviceName 안의 '%2F'가 '/'로 풀려 세그먼트 경계가 어긋난다.
   */
  pathname: string;
  /** '?'를 포함한 query string. 없으면 빈 문자열. */
  search: string;
  enableServiceMap: boolean;
  /**
   * servermap → servicemap으로 옮길 때 경로에 실을 service. 반대 방향에서는 쓰지 않는다.
   * 백엔드가 죽어 설정을 읽지 못하면 정해지지 않는데(`getRequestService`가 undefined),
   * 그때는 경로 빌더가 DEFAULT를 싣는다.
   */
  serviceName?: string;
}

/** 감춰진 화면이면 옮길 목적지를, 자기가 맡은 화면이 아니면 undefined를 반환한다. */
export type HiddenPageRule = (params: HiddenPageRedirectParams) => string | undefined;

/**
 * 세그먼트가 더 붙지 않는 **고정 경로 한 쌍**을 위한 규칙을 만든다. 경로를 다시 조립할 것이
 * 없으므로 어느 쪽이 감춰졌는지만 보면 된다. (query string은 목적지에도 싣는다 — 떨어뜨릴
 * 이유가 없다.)
 *
 * 경로에 application·serviceName 같은 세그먼트가 실리는 화면은 이 헬퍼로 만들 수 없다.
 * map(`getHiddenMapPageRedirect`)처럼 자기 규칙 함수를 따로 두고 목록에 넣는다.
 *
 * 인자 이름은 map이 아니라 **설정의 어느 쪽에서 보이는가**로 부른다. 이 헬퍼로 만드는 쌍은
 * map 화면이 아니므로(map은 자기 규칙 함수가 있다) servermap/servicemap이라 부르면 어긋난다.
 *
 * @param pageWhenDisabled 설정이 꺼져 있을 때 메뉴에 보이는 경로 (servermap 시절부터 있던 쪽)
 * @param pageWhenEnabled 설정이 켜져 있을 때 메뉴에 보이는 경로 (service 쪽)
 */
export const createHiddenPagePairRule = (
  pageWhenDisabled: string,
  pageWhenEnabled: string,
): HiddenPageRule => {
  return ({ pathname, search, enableServiceMap }) => {
    const hiddenPage = enableServiceMap ? pageWhenDisabled : pageWhenEnabled;
    const visiblePage = enableServiceMap ? pageWhenEnabled : pageWhenDisabled;

    // 하위 경로(`/config/auth/detail`)는 이 헬퍼가 맡는 화면이 아니므로 끝의 '/'만 떼고
    // 정확히 비교한다. 옮길 목적지는 넘겨받은 경로를 그대로 쓴다.
    return withoutTrailingSlash(pathname) === withoutTrailingSlash(hiddenPage)
      ? `${visiblePage}${search}`
      : undefined;
  };
};

/**
 * map 화면 한 쌍의 규칙 — `enableServiceMap` 설정에 따라 사이드 메뉴에서 감춘 map 화면의 URL을,
 * 남아 있는 쪽의 같은 화면으로 옮길 목적지를 정한다. 옮길 필요가 없으면 undefined다.
 *
 * | enableServiceMap | 보이는 메뉴 | 감춰서 옮기는 경로 |
 * |---|---|---|
 * | 켜짐 | servicemap | `/serverMap...` → `/serviceMap...` |
 * | 꺼짐 | servermap | `/serviceMap...` → `/serverMap...` |
 *
 * **메뉴에서 감추는 것만으로는 부족하다.** 링크·북마크·뒤로가기로 감춘 쪽 URL에 그대로 들어올 수
 * 있고, 그러면 메뉴에는 없는 화면이 그려진다. 첫 진입 경로(`/` → `/serverMap`)도 여기로 걸린다.
 *
 * 두 방향이 서로 반대 조건이라 리다이렉트가 되돌아오지 않는다 — 목적지에서 다시 물어보면
 * 같은 설정을 보고 "옮길 필요 없음"으로 판단한다. 이 규칙도 `getHiddenPageRedirect`의 목록에
 * 담긴 한 항목이다.
 */
export const getHiddenMapPageRedirect: HiddenPageRule = ({
  pathname,
  search,
  enableServiceMap,
  serviceName,
}: HiddenPageRedirectParams): string | undefined => {
  // 세그먼트가 더 긴 실시간 보기를 먼저 본다. `/serverMap/realtime`은 `/serverMap`의 하위
  // 경로라, 순서가 뒤바뀌면 'realtime'을 application 세그먼트로 읽는다.
  if (enableServiceMap) {
    const isServerMapRealtime = isUnderPage(pathname, APP_PATH.SERVER_MAP_REALTIME);

    if (!isServerMapRealtime && !isUnderPage(pathname, APP_PATH.SERVER_MAP)) {
      return undefined;
    }

    // DEFAULT가 아닌 service는 소속 application을 모두 모아 그려 기준 application이 없다.
    // 그대로 실어 보내면 목적지 로더가 곧 지우면서 한 번 더 움직인다.
    // service가 정해지지 않았으면 DEFAULT로 다룬다(경로 빌더가 싣는 값과 같다).
    const application =
      !serviceName || serviceName === DEFAULT_SERVICE ? getApplicationTypeAndName(pathname) : null;

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

/**
 * `enableServiceMap` 설정에 따라 **둘 중 하나만 메뉴에 보이는** 화면 쌍들.
 * service 도입이 끝날 때까지 이런 쌍이 늘어나므로 한 곳에 모아 둔다.
 *
 * | 쌍 | 꺼짐 (servermap 시절) | 켜짐 (service) |
 * |---|---|---|
 * | map | `/serverMap...` | `/serviceMap...` |
 */
const HIDDEN_PAGE_RULES: HiddenPageRule[] = [getHiddenMapPageRedirect];

/**
 * `enableServiceMap` 설정에 따라 사이드 메뉴에서 감춘 화면의 URL을, 남아 있는 쪽의 같은 화면으로
 * 옮길 목적지를 정한다. 옮길 필요가 없으면 undefined다.
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
 * | `resolveHiddenPageRedirect` (라우트 로더) | 화면에 들어올 때 | `getConfiguration` + localStorage |
 * | `useHiddenPageRedirect` (`InitialFetchOutlet`) | 이미 열린 화면에서 설정이 바뀔 때 | `useEnableServiceMap` |
 *
 * `extraRules`는 **이 저장소에 없는 화면**을 위한 자리다. 내려받아 쓰는 쪽(사내 배포판 등)에만
 * 있는 화면도 같은 설정으로 감춰지는데, 그 경로를 여기 적을 수는 없다. 규칙을 넘겨받으면
 * 들어오는 길과 이미 열린 탭 양쪽에 함께 적용되므로 한쪽만 빠뜨리는 일이 생기지 않는다.
 * 고정 경로 쌍이면 `createHiddenPagePairRule`로 만든다.
 */
export const getHiddenPageRedirect = (
  params: HiddenPageRedirectParams,
  extraRules: HiddenPageRule[] = [],
): string | undefined => {
  for (const rule of [...HIDDEN_PAGE_RULES, ...extraRules]) {
    const destination = rule(params);

    if (destination) {
      return destination;
    }
  }

  return undefined;
};
