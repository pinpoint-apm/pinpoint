import React from 'react';
import { getServiceMapPath, getServiceMapRealtimePath } from '@pinpoint-fe/ui/src/utils';
import { useIsDefaultService, useServiceNameForLink } from '@pinpoint-fe/ui/src/hooks';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { ServiceMap } from '../components/ServiceMap';
import { RealtimePage, RealtimePageProps } from './Realtime';

export type ServiceMapRealtimePageProps = Omit<
  RealtimePageProps,
  | 'MapView'
  | 'title'
  | 'getPagePath'
  | 'getRealtimePagePath'
  | 'requiresApplication'
  | 'serviceName'
>;

/**
 * servicemap의 실시간 보기.
 *
 * servermap의 실시간 보기와 화면이 완전히 같고, map을 그리는 API만 다르다
 * (`/api/servermap/serverMap` → `/api/servermap/serviceMap`). 그래서 화면을 복제하지 않고
 * `RealtimePage`에 map 컴포넌트만 갈아 끼운다.
 *
 * servicemap과 같은 두 모드가 여기에도 그대로 있다.
 * - DEFAULT: 고른 application 하나를 기준으로 map을 그린다. 그 application이 곧 우측 패널
 *   (스캐터/액티브 스레드)의 조회 대상이다.
 * - 그 외 service: 소속된 모든 application을 모아 map을 그린다. 기준 application이 없으므로
 *   우측 패널의 조회 대상은 map에서 노드를 클릭해 정한다.
 */
export const ServiceMapRealtimePage = (props: ServiceMapRealtimePageProps) => {
  // 경로에 serviceName이 실려 있으면 그것이, 없으면 전역 선택값이 이 화면의 service다.
  const serviceName = useServiceNameForLink() ?? DEFAULT_SERVICE;
  const isDefaultService = useIsDefaultService();

  // 실시간 보기를 벗어날 때(기간 선택, application 선택)도 같은 service의 servicemap으로 가야
  // 한다. serviceName이 빠지면 로더가 전역 선택값으로 다시 붙이는데, 그 값은 다른 탭에서
  // 바뀔 수 있어 보고 있던 service가 갈릴 수 있다.
  const getPagePath = React.useCallback(
    (application?: ApplicationType | null, queryParams?: { [k: string]: string }) =>
      getServiceMapPath(serviceName, application, queryParams),
    [serviceName],
  );

  const getRealtimePagePath = React.useCallback(
    (application?: ApplicationType | null) => getServiceMapRealtimePath(serviceName, application),
    [serviceName],
  );

  return (
    <RealtimePage
      {...props}
      MapView={ServiceMap}
      title="Servicemap"
      getPagePath={getPagePath}
      getRealtimePagePath={getRealtimePagePath}
      requiresApplication={isDefaultService}
      serviceName={serviceName}
    />
  );
};
