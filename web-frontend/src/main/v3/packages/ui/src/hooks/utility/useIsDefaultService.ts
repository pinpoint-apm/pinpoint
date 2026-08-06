import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import { useServiceNameForLink } from './useServiceNameForLink';

/**
 * 현재 화면이 조회하는 service가 DEFAULT인지 여부.
 *
 * DEFAULT도 다른 service와 똑같이 "선택된 service"다. 모든 조회에 pServiceName 헤더가 실리고,
 * 캐시도 service 단위로 갈린다. DEFAULT만 특별한 점은 **map이 어떤 application을 모으는가** 하나다.
 *
 * - DEFAULT: 고른 application 하나를 기준으로 map을 그린다. 그래서 먼저 골라야 한다.
 * - 그 외 service: 소속된 모든 application을 모아 map을 그린다. 고를 대상이 없다.
 *   (application이 많아 노드가 과도해지면 service를 나누도록 가이드한다.)
 *
 * 백엔드 `MapController.getSourceApplications`도 같은 규칙으로 source application을 정한다.
 *
 * enableServiceMap이 꺼져 있으면 백엔드가 모든 요청을 기본 service로 해석하므로 true다.
 * (`useServiceNameForLink`가 이때 undefined를 반환한다.)
 */
export const useIsDefaultService = () => {
  const serviceName = useServiceNameForLink();

  return (serviceName ?? DEFAULT_SERVICE) === DEFAULT_SERVICE;
};
