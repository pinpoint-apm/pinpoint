import { getDefaultStore } from 'jotai';
import { configurationAtom, selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import {
  getCurrentRouterPath,
  getEnableServiceMap,
  pickServiceName,
} from '@pinpoint-fe/ui/src/utils';

/**
 * 백엔드(service-module)의 `ServiceConstants.KEY`와 동일한 헤더 이름.
 * `HeaderServiceNameExtractor`가 이 헤더로 현재 선택된 service를 읽는다.
 */
export const SERVICE_NAME_HEADER = 'pServiceName';

const API_PATH_PREFIX = '/api';

/**
 * 렌더 밖에서 읽는 "이 요청이 나갈 service". 규칙은 `pickServiceName` 하나뿐이다
 * (enableServiceMap → 경로의 serviceName → 전역 선택값 → DEFAULT).
 *
 * 아래 fetch 인터셉터의 요청 헤더, 캐시 키(`serviceScopedQueryKeyHashFn`), 라우트 로더가 모두
 * 이 함수를 쓴다. 서로 다른 규칙을 쓰면 헤더는 A service로 나가는데 캐시는 B service 키에 쌓여
 * 다른 service의 데이터가 섞인다.
 *
 * 경로는 주소창(`window.location`)이 아니라 **라우터가 렌더한 경로**(`getCurrentRouterPath`)에서
 * 읽는다. 뒤로/앞으로 가기에서 주소창이 라우터보다 앞서가면 조회 파라미터는 이전 경로의 것인데
 * service만 새 경로의 것이 되어 같은 조회가 두 번 나간다. (이슈 #10587)
 *
 * configuration과 localStorage는 부트스트랩 이후에도 바뀌므로 값을 캡처하지 않고 부를 때마다
 * 최신값을 읽는다. 화면용 갈래는 `useRequestService`다.
 */
export const getRequestService = (): string | undefined => {
  const store = getDefaultStore();

  return pickServiceName({
    enableServiceMap: getEnableServiceMap(store.get(configurationAtom)),
    pathname: getCurrentRouterPath(),
    selectedService: store.get(selectedServiceAtom),
  });
};

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
 * 전역 `fetch`를 한 번 래핑하여, 백엔드로 가는 `/api` 요청에 `pServiceName` 헤더를 싣는다.
 *
 * **어느 service로 나갈지는 이 순서로 정한다.**
 *
 * 1. 요청에 `pServiceName`이 이미 실려 있으면 → **건드리지 않고 그대로 보낸다.**
 *    호출자가 직접 실은 값이 항상 이긴다. 인터셉터는 경로와 전역 선택값만 보므로 "고른 노드가
 *    화면과 다른 service 소속"이라는 사실(`useServerMapTargetServiceName`)을 알 수 없다.
 *    이 규칙은 `enableServiceMap`이 꺼져 있어도 그대로다 — 호출자가 실은 헤더를 지우지는 않는다.
 * 2. 그 외에는 `getRequestService()`가 정한다(enableServiceMap → 경로 → 전역 선택값 → DEFAULT).
 *    설정이 꺼져 있으면 undefined이므로 헤더를 싣지 않고, 백엔드가 기본 service로 해석한다.
 *
 * 캐시 키(`serviceScopedQueryKeyHashFn`)도 같은 함수를 지난다 — 다르면 헤더는 A service로
 * 나가는데 캐시는 B service 키에 쌓인다.
 *
 * configuration과 localStorage는 모두 부트스트랩 이후 바뀔 수 있으므로, 값을 캡처하지 않고
 * 매 요청 시 최신값을 읽는다(`getRequestService`가 Jotai 기본 store에서 직접 읽으므로
 * 컴포넌트의 `useAtomValue`/`useSetAtom`과 동일한 상태를 참조한다).
 * 앱 부트스트랩(main.tsx)에서 렌더링 전에 한 번 호출한다.
 */
export const installServiceNameFetchInterceptor = () => {
  if (installed) return;
  if (typeof window === 'undefined' || typeof window.fetch !== 'function') return;
  installed = true;

  const originalFetch = window.fetch.bind(window);

  window.fetch = (input: RequestInfo | URL, init?: RequestInit) => {
    try {
      if (!isApiRequest(input)) {
        return originalFetch(input, init);
      }

      const headers = new Headers(
        init?.headers ?? (isRequestObject(input) ? input.headers : undefined),
      );

      // 1) 이미 실려 있으면 그대로 보낸다. 헤더를 다시 만들지도 않는다.
      if (headers.has(SERVICE_NAME_HEADER)) {
        return originalFetch(input, init);
      }

      // 2) 경로의 serviceName → 3) 전역 선택값 → 4) DEFAULT.
      //    설정이 꺼져 있으면 undefined다 — 그때는 헤더를 싣지 않는다.
      const requestService = getRequestService();

      if (!requestService) {
        return originalFetch(input, init);
      }

      headers.set(SERVICE_NAME_HEADER, requestService);
      return originalFetch(input, { ...init, headers });
    } catch {
      // 인터셉터 내부 오류가 요청 자체를 막지 않도록 원본 fetch로 폴백한다.
      return originalFetch(input, init);
    }
  };
};
