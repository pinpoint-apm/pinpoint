import { useLocation } from 'react-router-dom';
import { useAtomValue } from 'jotai';
import { selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';
import { getServiceNameFromPath } from '@pinpoint-fe/ui/src/utils';
import { useConfiguration } from './useConfiguration';

/**
 * 다른 화면으로 넘기는 링크(`getTransactionListPath`)에 실을 service 이름.
 * 실을 필요가 없으면 undefined를 반환하므로 링크 형태는 그대로 유지된다.
 *
 * configuration은 `configurationAtom`에서 직접 읽는다. 저장소는 그 atom 하나뿐이므로
 * 확장 저장소(naver 등)에서 이 파일을 그대로 복사해도 같은 값을 본다.
 *
 * 판단 규칙은 요청 헤더/캐시 키와 같은 `resolveRequestService`를 그대로 따른다.
 * - enableServiceMap이 꺼져 있으면 service 개념 자체가 없으므로 싣지 않는다.
 * - 켜져 있으면 화면별 예외 없이(ServerMap 포함) 현재 service를 싣는다.
 * - 이미 URL에 serviceName이 실려 있으면(transactionList) 전역 선택값보다 그것을 우선해,
 *   화면 안에서 이동해도 처음 열었을 때의 service가 유지된다.
 */
export const useServiceNameForLink = () => {
  const configuration = useConfiguration();
  const selectedService = useAtomValue(selectedServiceAtom);
  const { pathname } = useLocation();

  if (!configuration?.['experimental.enableServiceMap.value']) {
    return undefined;
  }

  return getServiceNameFromPath(pathname) || selectedService;
};
