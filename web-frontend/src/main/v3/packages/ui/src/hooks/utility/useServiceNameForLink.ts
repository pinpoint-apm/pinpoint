import { useLocation } from 'react-router-dom';
import { useAtomValue } from 'jotai';
import { selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';
import { getServiceNameFromPath } from '@pinpoint-fe/ui/src/utils';

/**
 * 다른 화면으로 넘기는 링크(`getTransactionListPath`)에 실을 service 이름.
 * 실을 필요가 없으면 undefined를 반환하므로 링크 형태는 그대로 유지된다.
 *
 * configuration은 이 저장소의 관례대로 호출부에서 받는다. 페이지가 web 앱에서 받은
 * configuration을 prop으로 내려주므로, ui 패키지가 `configurationAtom`에 직접 의존하지 않는다.
 *
 * 판단 규칙은 요청 헤더/캐시 키와 같은 `resolveRequestService`를 그대로 따른다.
 * - enableServiceMap이 꺼져 있으면 service 개념 자체가 없으므로 싣지 않는다.
 * - 켜져 있으면 화면별 예외 없이(ServerMap 포함) 현재 service를 싣는다.
 * - 이미 URL에 serviceName이 실려 있으면(transactionList) 전역 선택값보다 그것을 우선해,
 *   화면 안에서 이동해도 처음 열었을 때의 service가 유지된다.
 */
export const useServiceNameForLink = (configuration?: Configuration) => {
  const selectedService = useAtomValue(selectedServiceAtom);
  const { pathname } = useLocation();

  if (!configuration?.['experimental.enableServiceMap.value']) {
    return undefined;
  }

  return getServiceNameFromPath(pathname) || selectedService;
};
