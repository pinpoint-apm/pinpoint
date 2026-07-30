import { getDefaultStore } from 'jotai';
import { DEFAULT_SERVICE, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, BASE_PATH, Configuration } from '@pinpoint-fe/ui/src/constants';

/**
 * 백엔드(service-module)의 `ServiceConstants.KEY`와 동일한 헤더 이름.
 * `HeaderServiceNameExtractor`가 이 헤더로 현재 선택된 service를 읽는다.
 */
export const SERVICE_NAME_HEADER = 'pServiceName';

const API_PATH_PREFIX = '/api';

/**
 * `window.location.pathname`에는 라우터 basename(BASE_PATH)이 포함되므로, 라우터가 보는
 * 경로와 비교하려면 접두사를 떼어내야 한다. BASE_PATH가 비어 있으면 그대로 반환한다.
 */
const toRouterPath = (pathname: string) =>
  BASE_PATH && pathname.startsWith(BASE_PATH) ? pathname.slice(BASE_PATH.length) : pathname;

/**
 * service 개념에서 제외되는 경로. enableServiceMap이 켜져 있으면 service 헤더를 붙이는 것이
 * 기본이고, ServerMap만 예외적으로 제외한다. ServerMap은 service 개념이 도입되기 전의
 * 화면이라 다른 페이지와 같은 API를 쓰더라도 헤더 없이 보내 기본 service로 조회해야 한다.
 * 추후 ServiceMap이 ServerMap을 대체하면 이 함수와 호출부를 함께 지우면 된다.
 */
export const isServiceExcludedPath = (pathname: string = window.location.pathname) =>
  toRouterPath(pathname).startsWith(APP_PATH.SERVER_MAP);

/**
 * 이 요청이 실제로 어떤 service로 해석되는지 반환한다. 제외 경로에서는 헤더를 생략해
 * 백엔드가 기본 service로 해석하므로 `DEFAULT_SERVICE`다.
 *
 * 요청 헤더(아래 인터셉터)와 캐시 키(reactQueryHelper의 `serviceScopedQueryKeyHashFn`)는
 * 반드시 같은 규칙에서 파생되어야 한다. 서로 다른 규칙을 쓰면 헤더는 A service로 나가는데
 * 캐시는 B service 키에 쌓여 다른 service의 데이터가 섞인다.
 */
export const resolveRequestService = (selectedService: string) =>
  isServiceExcludedPath() ? DEFAULT_SERVICE : selectedService;

/** `resolveRequestService`를 현재 선택된 service에 적용한 값. 렌더 밖(모듈 레벨)에서 쓴다. */
export const getRequestService = () =>
  resolveRequestService(getDefaultStore().get(selectedServiceAtom));

const isRequestObject = (input: RequestInfo | URL): input is Request =>
  typeof Request !== 'undefined' && input instanceof Request;

const getRequestUrl = (input: RequestInfo | URL): string => {
  if (typeof input === 'string') return input;
  if (input instanceof URL) return input.href;
  if (isRequestObject(input)) return input.url;
  return String(input);
};

const isApiRequest = (input: RequestInfo | URL): boolean => {
  try {
    const { pathname } = new URL(getRequestUrl(input), window.location.origin);
    return pathname.startsWith(API_PATH_PREFIX);
  } catch {
    return false;
  }
};

let installed = false;

/**
 * 전역 `fetch`를 한 번 래핑하여, configuration의
 * `experimental.enableServiceMap.value`가 true일 때 백엔드로 가는 모든
 * `/api` 요청 헤더에 현재 선택된 service(`selectedServiceAtom`)를 주입한다.
 * 단, `isServiceExcludedPath`에 해당하는 경로에서는 예외적으로 주입하지 않는다.
 *
 * configuration은 부트스트랩 이후 비동기로 로드/갱신되므로, 값이 아니라
 * 매 요청 시 최신 configuration을 반환하는 getter(`getConfiguration`)를 받는다.
 * 이를 통해 ui 패키지가 `configurationAtom`에 직접 의존하지 않고,
 * web 앱이 configuration의 출처를 주입한다.
 *
 * service는 Jotai 기본 store(`getDefaultStore`)에서 읽으므로 컴포넌트의
 * `useAtomValue`/`useSetAtom`과 동일한 상태를 참조한다.
 * 앱 부트스트랩(main.tsx)에서 렌더링 전에 한 번 호출한다.
 */
export const installServiceNameFetchInterceptor = (
  getConfiguration: () => Configuration | undefined,
) => {
  if (installed) return;
  if (typeof window === 'undefined' || typeof window.fetch !== 'function') return;
  installed = true;

  const store = getDefaultStore();
  const originalFetch = window.fetch.bind(window);

  window.fetch = (input: RequestInfo | URL, init?: RequestInit) => {
    try {
      const configuration = getConfiguration();
      const enableServiceMap = !!configuration?.['experimental.enableServiceMap.value'];

      if (enableServiceMap && isApiRequest(input) && !isServiceExcludedPath()) {
        const selectedService = store.get(selectedServiceAtom);

        if (selectedService) {
          const headers = new Headers(
            init?.headers ?? (isRequestObject(input) ? input.headers : undefined),
          );
          headers.set(SERVICE_NAME_HEADER, selectedService);
          return originalFetch(input, { ...init, headers });
        }
      }
    } catch {
      // 인터셉터 내부 오류가 요청 자체를 막지 않도록 원본 fetch로 폴백한다.
    }

    return originalFetch(input, init);
  };
};
