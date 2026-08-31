import React from 'react';
import { useLocation } from 'react-router';
import { useAtom, useAtomValue } from 'jotai';
import { selectedServiceAtom, servicesAtom } from '@pinpoint-fe/ui/src/atoms';
import { getServiceNameFromPath } from '@pinpoint-fe/ui/src/utils';

/**
 * 경로에 실린 serviceName을 전역 선택값(`selectedServiceAtom`)에 반영하고, 그 이름이 존재하지
 * 않는 service인지 알려준다.
 *
 * 경로가 진실의 원천이므로 방향이 이쪽이다. 주소창을 직접 고쳐 들어오거나, 다른 service의
 * serviceName이 실린 링크를 열면 화면은 그 service를 보는데 사이드바 표시와 다음 화면 링크는
 * 이전 service를 가리켜 어긋난다. (조회 자체는 경로 기준이라 이미 새 service로 나간다.)
 *
 * 선택값은 **존재하는 service로 확인된 경로에서만** 반영한다. 없는 이름을 반영하면 그 값이
 * 요청 헤더와 캐시 키로까지 퍼진다.
 *
 * service 목록은 비동기로 오므로 도착 전에는 존재 여부를 판단하지 않는다. 목록이 채워지면 다시
 * 계산된다. (목록이 없을 때 없는 service로 단정하면 새로고침마다 정상적인 service가 404로 보인다.)
 *
 * enabled(enableServiceMap)가 아니면 service 개념 자체가 없으므로 아무 것도 하지 않는다.
 */
export const useSyncSelectedServiceWithPath = (enabled: boolean) => {
  const { pathname } = useLocation();
  const services = useAtomValue(servicesAtom);
  const [selectedService, setSelectedService] = useAtom(selectedServiceAtom);
  const serviceNameInPath = getServiceNameFromPath(pathname);

  /**
   * 경로가 존재하지 않는 service를 가리키는지. 그 이름으로는 어떤 조회도 의미가 없으므로 호출자가
   * 화면 대신 404를 그린다. 다른 service로 조용히 바꿔 보여주면 사용자는 자기가 요청한 것과
   * 다른 것을 보고 있는 줄 모른다.
   */
  const isUnknownServiceInPath =
    enabled && !!serviceNameInPath && !!services && !services.includes(serviceNameInPath);

  React.useEffect(() => {
    if (!enabled || !serviceNameInPath || serviceNameInPath === selectedService) {
      return;
    }

    if (!services?.includes(serviceNameInPath)) {
      return;
    }

    setSelectedService(serviceNameInPath);
  }, [enabled, serviceNameInPath, selectedService, services, setSelectedService]);

  return { isUnknownServiceInPath };
};
