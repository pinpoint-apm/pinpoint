import { matchRoutes, RouteObject } from 'react-router';
import { APP_PATH, ApplicationType } from '@pinpoint-fe/ui/src/constants';

export const getApplicationTypeAndName = (path = '') => {
  const splittedPath = path.match(/\/?([^/]+)[@|^]([^/]+)$/);
  const applicationName = splittedPath?.[1];
  const serviceType = splittedPath?.[2];

  if (applicationName && serviceType) {
    return { applicationName, serviceType };
  }

  return null;
};

/**
 * URL에 실린 serviceName을 디코딩한다(경로 빌더가 encodeURIComponent로 싣는다).
 * 경로는 사용자가 직접 편집할 수 있어 '%' 하나만 있는 등 잘못된 인코딩이 들어올 수 있고,
 * 이 함수는 렌더 중에 호출되므로 던지면 화면이 죽는다. 실패하면 원본을 그대로 쓴다.
 */
const decodeServiceName = (serviceName: string) => {
  try {
    return decodeURIComponent(serviceName);
  } catch {
    return serviceName;
  }
};

/**
 * ─── 경로에 실리는 serviceName ───────────────────────────────────────────────
 *
 * 경로는 serviceName을 싣는다. 어떤 service를 보던 중이었는지 URL에 남아 있어야 그 화면의
 * 모든 API에 pServiceName 헤더를 실을 수 있다. 링크를 새 탭으로 열면 전역 선택값
 * (`selectedServiceAtom`)은 탭 간 공유 저장소라 이미 다른 값일 수 있어 믿을 수 없다.
 *
 * 표기는 **세그먼트 표기** 하나다 — `/{page}/{serviceName}/{applicationName}@{serviceType}?`
 * serviceName이 독립 세그먼트라 application이 없어도 실을 수 있고(service 전체를 그리는
 * servicemap), application 세그먼트를 건드리지 않아 그 안의 '@'와 뒤섞이지도 않는다.
 *
 * **어느 화면이 serviceName 세그먼트를 싣는지는 라우트 정의가 정한다.** 라우트 경로에
 * `:serviceName` 파라미터가 있으면 그 자리가 serviceName이다. 경로의 첫 세그먼트는 화면마다 뜻이
 * 달라서(`/serverMap/{app}@{type}`, `/config/alarm`, `/serviceMap/realtime/...`) 모양으로는
 * 가릴 수 없다. 화면 목록을 따로 두면 라우트에 세그먼트를 추가할 때 목록도 함께 고쳐야 하고,
 * 빠뜨리면 경로에는 A service가 적혀 있는데 헤더와 캐시 키는 전역 선택값인 B service로 나간다.
 *
 * 라우트에 `:serviceName`이 없는 화면(servermap 계열, config 화면들)에서는 serviceName을 읽을 수
 * 없어 전역 선택값으로 폴백한다(`getRequestService`).
 */

/** 라우트 경로에서 serviceName 자리를 나타내는 파라미터. */
const SERVICE_NAME_PARAM = ':serviceName';

let appRoutes: RouteObject[] = [];
let lastResolved: { pathname: string; page: string | undefined } | undefined;

/**
 * 앱의 라우트 정의를 등록한다. 어느 화면이 serviceName 세그먼트를 싣는지를 이 정의에서 읽는다.
 *
 * `createBrowserRouter`에 넘기는 **같은 배열**을, 첫 라우트 로더와 첫 렌더보다 먼저 한 번 넘긴다
 * (`apps/web/src/routes`). 이 패키지 밖(앱)의 화면도 라우트에 `:serviceName`을 두면 그대로
 * 인식된다. 등록하지 않으면 어느 경로에서도 serviceName을 읽지 못해 전역 선택값으로 폴백한다
 * (깨지지는 않고, URL의 service가 무시될 뿐이다).
 */
export const registerAppRoutes = (routes: RouteObject[]) => {
  appRoutes = routes;
  lastResolved = undefined;
};

/** 중첩 라우트의 경로를 이어 붙인다. '/'로 시작하는 자식 경로는 그 자체가 전체 경로다. */
const joinRoutePath = (parentPath: string, path?: string) => {
  if (!path) {
    return parentPath;
  }

  if (path.startsWith('/')) {
    return path;
  }

  return `${parentPath.replace(/\/$/, '')}/${path}`;
};

const findServiceNameSegmentPage = (pathname: string) => {
  const matches = matchRoutes(appRoutes, pathname);

  if (!matches) {
    return undefined;
  }

  const segments = matches
    .reduce((routePath, { route }) => joinRoutePath(routePath, route.path), '')
    .split('/');
  // optional 표기('?')를 떼고 비교한다. `:serviceNameX` 같은 다른 파라미터와는 구별된다.
  const serviceNameIndex = segments.findIndex(
    (segment) => segment.replace(/\?$/, '') === SERVICE_NAME_PARAM,
  );

  return serviceNameIndex < 0 ? undefined : segments.slice(0, serviceNameIndex).join('/');
};

/**
 * pathname이 속한, serviceName 세그먼트를 싣는 페이지의 경로 접두사.
 *
 * 라우트 로더가 리다이렉트 목적지를 만들 때 쓴다. 로더는 여러 라우트가 공유하므로
 * (`scatterOrHeatmapFullScreenLoader`는 scatter/heatmap 두 경로에 걸려 있다) 접두사를
 * 경로에서 되찾아야 하는데, 그 판정이 serviceName을 읽는 규칙과 갈리면
 * "serviceName은 읽었는데 리다이렉트 목적지에는 빠지는" 어긋남이 생긴다.
 *
 * 매칭된 라우트 경로에서 `:serviceName` 앞까지를 돌려준다(`/serviceMap/realtime/:serviceName?/...`
 * → `/serviceMap/realtime`). 하위 경로끼리의 우선순위(`/serviceMap/realtime` ↔ `/serviceMap`)는
 * react-router의 경로 랭킹이 정하므로, 라우터가 실제로 고르는 라우트와 언제나 같다.
 *
 * 라우트를 고를 때만 react-router를 쓰고 serviceName 자체는 아래에서 원본(raw) 세그먼트로 읽는다.
 * 매칭 결과의 `params`는 디코딩된 값이라 '%40'이 '@'로 풀려, service 이름 `a@b`와 application
 * 세그먼트 `app@TOMCAT`을 구별할 수 없게 된다.
 *
 * 요청마다·렌더마다 불리므로 마지막 결과 하나를 기억한다.
 */
export const getServiceNameSegmentPage = (pathname = '') => {
  if (lastResolved?.pathname !== pathname) {
    lastResolved = { pathname, page: findServiceNameSegmentPage(pathname) };
  }

  return lastResolved.page;
};

/**
 * 경로에 실려 있는 serviceName. 아직 serviceName을 싣지 않는 화면에서는 undefined이므로,
 * 호출자가 전역 선택값으로 폴백한다.
 *
 * serviceName 세그먼트가 생기기 전에는 이 자리에 application이 있었다(`/{page}/{app}@{type}`).
 * 그런 옛 링크·북마크를 service 이름으로 오해하지 않도록, `{app}@{type}`으로 파싱되는 세그먼트는
 * serviceName이 아닌 것으로 본다. serviceName은 encodeURIComponent로 실려서 '@'와 '^'가 각각
 * '%40'/'%5E'가 되므로, 구분자가 그대로 남아 있다면 application 세그먼트다.
 *
 * 인코딩된(raw) pathname을 넘겨야 한다. 경로 빌더가 encodeURIComponent로 실으므로,
 * 디코딩된 값을 넘기면 serviceName 안의 '%2F'가 '/'로 풀려 세그먼트 경계가 어긋난다.
 */
export const getServiceNameFromPath = (pathname = '') => {
  const page = getServiceNameSegmentPage(pathname);

  if (!page) {
    return undefined;
  }

  const [serviceNameSegment] = pathname.slice(page.length).replace(/^\//, '').split('/');

  if (!serviceNameSegment || getApplicationTypeAndName(serviceNameSegment)) {
    return undefined;
  }

  return decodeServiceName(serviceNameSegment);
};

/** 경로가 serviceName을 싣고 있는지 여부. */
export const hasServiceNameInPath = (pathname = '') => !!getServiceNameFromPath(pathname);

/**
 * `/{page}/{serviceName}/{applicationName}@{serviceType}?` 형태의 경로를 세그먼트로 분해한다.
 * servicemap 계열 라우트 로더가 리다이렉트 목적지를 만들 때 쓴다.
 *
 * **인코딩된(raw) pathname을 넘겨야 한다.** react-router의 `params`는 디코딩된 값이라
 * serviceName 안의 '%2F'가 '/'로 풀려 세그먼트 경계가 어긋난다.
 *
 * 첫 세그먼트가 serviceName인지의 판정은 화면과 같은 함수(`getServiceNameFromPath`)에 맡긴다.
 * serviceName이 아니라고 판정되면(세그먼트가 생기기 전 형태의 옛 링크) 첫 세그먼트를
 * application으로 읽는다.
 *
 * @param pagePath 이 경로의 페이지 접두사(`APP_PATH.SERVICE_MAP` 등). pathname이 이 접두사
 *                 아래에 있어야 한다.
 */
export const parseServiceScopedPath = (pagePath: string, pathname = '') => {
  const [firstSegment, secondSegment] = pathname
    .slice(pagePath.length)
    .replace(/^\//, '')
    .split('/');

  const serviceName = getServiceNameFromPath(pathname);
  const applicationSegment = serviceName ? secondSegment : firstSegment;

  return {
    serviceName,
    /** 리다이렉트 목적지를 만들 때 쓰는, 인코딩된 그대로의 serviceName 세그먼트. */
    encodedServiceName: serviceName ? firstSegment : undefined,
    applicationSegment,
    application: getApplicationTypeAndName(applicationSegment),
  };
};

/**
 * `/systemMetric/{serviceName}?/{hostGroupName}?` 경로를 분해한다.
 *
 * 다른 화면은 첫 세그먼트가 `{app}@{type}`으로 파싱되는지로 serviceName 여부를 가리지만,
 * hostGroup은 구분자가 없는 이름이라 모양만으로는 `/systemMetric/X`의 X가 service인지 hostGroup인지
 * 알 수 없다. 그래서 **경로 형태를 `enableServiceMap`이 정한다.**
 *
 * - 켜져 있으면 첫 세그먼트는 언제나 serviceName, 두 번째가 hostGroup이다. 세그먼트가 생기기 전
 *   형태(`/systemMetric/{hostGroup}`)는 라우트 로더가 service 목록과 대조해 표준 형태로 옮긴다.
 * - 꺼져 있으면 service 개념이 없으므로 마지막 세그먼트가 hostGroup이다(예전과 같은 규칙). 설정이
 *   켜진 채 만들어진 `/systemMetric/{serviceName}/{hostGroup}` 링크도 그대로 열린다.
 *
 * hostGroup은 예전처럼 경로의 값을 그대로 쓴다(디코딩하지 않는다). serviceName은 인코딩된 채로
 * 돌려준다 — 리다이렉트 목적지에 그대로 다시 싣기 위해서다. 디코딩된 값은 `getServiceNameFromPath`.
 */
export const parseSystemMetricPath = (pathname = '', enableServiceMap: boolean) => {
  const segments = pathname
    .slice(APP_PATH.SYSTEM_METRIC.length)
    .split('/')
    .filter((segment) => !!segment);

  if (enableServiceMap) {
    return { encodedServiceName: segments[0], hostGroupName: segments[1] };
  }

  return { encodedServiceName: undefined, hostGroupName: segments[segments.length - 1] };
};

export const getApplicationKey = (application?: ApplicationType) => {
  return `${application?.applicationName}^${application?.serviceType}`;
};
