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

/** 라우터 기준 pathname(basename 제외)이 해당 페이지의 경로인지 여부. */
const isUnderPage = (pathname: string, pagePath: string) =>
  pathname === pagePath || pathname.startsWith(`${pagePath}/`);

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
 * 아직 serviceName을 싣지 않는 화면도 남아 있다. 그 경로에서는 serviceName을 읽을 수 없어
 * 전역 선택값으로 폴백한다(`resolveRequestService`). 화면이 옮겨질 때마다
 * `SERVICE_NAME_SEGMENT_PAGES`에 추가하면 되고, 폴백에 의존하는 화면은 그만큼 줄어든다.
 */

/**
 * serviceName을 세그먼트로 싣는 페이지.
 *
 * 앞에서부터 처음 매칭되는 항목을 쓰므로(`find`) 더 긴 경로를 먼저 둔다. `/serviceMap/realtime`은
 * `/serviceMap`의 하위 경로라, 순서가 뒤바뀌면 'realtime' 세그먼트를 serviceName으로 읽는다.
 */
const SERVICE_NAME_SEGMENT_PAGES: string[] = [
  APP_PATH.SERVICE_MAP_REALTIME,
  APP_PATH.SERVICE_MAP,
  APP_PATH.FILTERED_MAP,
  APP_PATH.TRANSACTION_LIST,
  APP_PATH.TRANSACTION_DETAIL,
];

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
  const page = SERVICE_NAME_SEGMENT_PAGES.find((pagePath) => isUnderPage(pathname, pagePath));

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

export const getApplicationKey = (application?: ApplicationType) => {
  return `${application?.applicationName}^${application?.serviceType}`;
};
