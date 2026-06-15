import { getDefaultStore } from 'jotai';
import { configurationAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';

/**
 * 백엔드(service-module)의 `ServiceConstants.KEY`와 동일한 헤더 이름.
 * `HeaderServiceNameExtractor`가 이 헤더로 현재 선택된 service를 읽는다.
 */
export const SERVICE_NAME_HEADER = 'pServiceName';

const API_PATH_PREFIX = '/api';

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
 *
 * Jotai 기본 store(`getDefaultStore`)를 사용하므로 컴포넌트의
 * `useAtomValue`/`useSetAtom`과 동일한 상태를 참조한다.
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
