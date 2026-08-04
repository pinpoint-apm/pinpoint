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
 * application 세그먼트에 serviceName까지 함께 싣는 페이지 목록.
 * 이 경로의 세그먼트는 `{serviceName}@{applicationName}@{serviceType}` 형태다.
 *
 * transactionList는 scatter/heatmap의 drag&drop으로, transactionDetail은 transactionList의
 * 외부 링크 버튼으로 새 탭에서 열리므로, 어떤 service를 보고 있었는지 URL에 남겨두지 않으면
 * 모든 API에 실어야 할 pServiceName 헤더를 복원할 수 없다.
 * (전역 선택값은 탭 간 공유 저장소라서 링크를 열 시점의 값과 다를 수 있다.)
 */
const SERVICE_NAME_IN_PATH_PAGES: string[] = [
  APP_PATH.TRANSACTION_LIST,
  APP_PATH.TRANSACTION_DETAIL,
];

/**
 * 라우터 기준 pathname(basename 제외)이 serviceName을 싣는 경로인지 여부.
 * applicationName 자체에 '@'가 들어갈 수 있으므로, serviceName 분리는 이 경로에서만 해야 한다.
 */
export const hasServiceNameInPath = (pathname = '') =>
  SERVICE_NAME_IN_PATH_PAGES.some((page) => pathname === page || pathname.startsWith(`${page}/`));

/**
 * URL에 실린 serviceName을 디코딩한다(`getServiceScopedApplicationPath`가 인코딩한다).
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
 * `{serviceName}@{applicationName}@{serviceType}` 세그먼트를 분해한다.
 * serviceName이 없는 기존 형태(`{applicationName}@{serviceType}`)면 serviceName은 undefined다.
 *
 * serviceName은 첫 '@' 앞부분으로, applicationName/serviceType은 기존 규칙(마지막 구분자)을
 * 그대로 따른다. 경로 전체를 넘겨도 되고 세그먼트만 넘겨도 된다.
 *
 * 인코딩된(raw) 경로를 넘겨야 한다. react-router의 `params`는 디코딩된 값이므로 그대로 쓰면
 * serviceName의 '%2F'가 '/'로 풀려 세그먼트 경계가 어긋난다(`transactionRouteLoader` 참고).
 */
export const getServiceAndApplicationTypeAndName = (path = '') => {
  const application = getApplicationTypeAndName(path);

  if (!application) {
    return null;
  }

  const separatorIndex = application.applicationName.indexOf('@');

  if (separatorIndex < 1) {
    return { serviceName: undefined, ...application };
  }

  return {
    serviceName: decodeServiceName(application.applicationName.slice(0, separatorIndex)),
    applicationName: application.applicationName.slice(separatorIndex + 1),
    serviceType: application.serviceType,
  };
};

/**
 * pathname에서 application을 읽는다. serviceName이 실리는 경로에서는 serviceName을 떼어내고,
 * 그 외에는 기존 규칙 그대로 파싱해 applicationName에 '@'가 포함된 경우가 깨지지 않게 한다.
 */
export const getApplicationTypeAndNameFromPath = (pathname = '') => {
  if (!hasServiceNameInPath(pathname)) {
    return getApplicationTypeAndName(pathname);
  }

  const parsed = getServiceAndApplicationTypeAndName(pathname);

  return parsed && { applicationName: parsed.applicationName, serviceType: parsed.serviceType };
};

/** pathname에 실려 있는 serviceName. serviceName을 싣지 않는 경로에서는 undefined. */
export const getServiceNameFromPath = (pathname = '') =>
  hasServiceNameInPath(pathname)
    ? getServiceAndApplicationTypeAndName(pathname)?.serviceName
    : undefined;

export const getApplicationKey = (application?: ApplicationType) => {
  return `${application?.applicationName}^${application?.serviceType}`;
};
