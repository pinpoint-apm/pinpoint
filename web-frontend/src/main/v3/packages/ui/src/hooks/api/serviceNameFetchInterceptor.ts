import { getDefaultStore } from 'jotai';
import { configurationAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { BASE_PATH } from '@pinpoint-fe/ui/src/constants';
import { getServiceNameFromPath } from '@pinpoint-fe/ui/src/utils';

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
 * 이 요청이 실제로 어떤 service로 해석되는지 반환한다. enableServiceMap이 켜져 있으면
 * 예외 없이 모든 화면(ServerMap 포함)이 선택된 service 범위에서 조회된다.
 *
 * 경로에 serviceName이 실려 있으면(`getServiceNameFromPath`) 전역 선택값
 * (`selectedServiceAtom`)보다 그것을 우선한다. 전역 선택값은 탭 간 공유 저장소라서, 링크를 새 탭에
 * 열어 둔 뒤 원래 탭에서 service를 바꾸면 화면과 어긋난다. 아직 serviceName을 싣지 않는 화면만
 * 전역 선택값으로 폴백한다.
 *
 * 요청 헤더(아래 인터셉터)와 캐시 키(reactQueryHelper의 `serviceScopedQueryKeyHashFn`)는
 * 반드시 같은 규칙에서 파생되어야 한다. 서로 다른 규칙을 쓰면 헤더는 A service로 나가는데
 * 캐시는 B service 키에 쌓여 다른 service의 데이터가 섞인다.
 */
export const resolveRequestService = (selectedService: string) =>
  getServiceNameFromPath(toRouterPath(window.location.pathname)) || selectedService;

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
 * `/api` 요청 헤더에 그 요청이 해석되는 service(`resolveRequestService`)를 주입한다.
 * 화면별 예외는 없다. 설정이 꺼져 있으면 헤더를 아예 붙이지 않아 백엔드가 기본 service로 해석한다.
 *
 * configuration은 부트스트랩 이후 비동기로 로드/갱신되므로, 값을 캡처하지 않고
 * 매 요청 시 `configurationAtom`에서 최신값을 읽는다.
 *
 * configuration과 service 모두 Jotai 기본 store(`getDefaultStore`)에서 읽으므로
 * 컴포넌트의 `useAtomValue`/`useSetAtom`과 동일한 상태를 참조한다.
 * 앱 부트스트랩(main.tsx)에서 렌더링 전에 한 번 호출한다.
 */
export const installServiceNameFetchInterceptor = () => {
  if (installed) return;
  if (typeof window === 'undefined' || typeof window.fetch !== 'function') return;
  installed = true;

  const store = getDefaultStore();
  const originalFetch = window.fetch.bind(window);

  window.fetch = (input: RequestInfo | URL, init?: RequestInit) => {
    try {
      const configuration = store.get(configurationAtom);
      const enableServiceMap = !!configuration?.['experimental.enableServiceMap.value'];

      if (enableServiceMap && isApiRequest(input)) {
        // 캐시 키와 어긋나지 않도록 헤더도 `resolveRequestService`와 같은 규칙에서 파생한다.
        const selectedService = resolveRequestService(store.get(selectedServiceAtom));

        if (selectedService) {
          const headers = new Headers(
            init?.headers ?? (isRequestObject(input) ? input.headers : undefined),
          );

          // 호출자가 직접 실은 값이 우선이다. 인터셉터는 경로/전역 선택값만 보므로, 조회 대상이
          // 화면과 다른 service에 속한다는 사실(`useServerMapTargetServiceName`)을 알 수 없다.
          if (!headers.has(SERVICE_NAME_HEADER)) {
            headers.set(SERVICE_NAME_HEADER, selectedService);
          }
          return originalFetch(input, { ...init, headers });
        }
      }
    } catch {
      // 인터셉터 내부 오류가 요청 자체를 막지 않도록 원본 fetch로 폴백한다.
    }

    return originalFetch(input, init);
  };
};
