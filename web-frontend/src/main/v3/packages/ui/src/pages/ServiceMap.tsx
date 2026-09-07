import React from 'react';
import { getServiceMapPath, getServiceMapRealtimePath } from '@pinpoint-fe/ui/src/utils';
import { useIsDefaultService, useServiceNameForLink } from '@pinpoint-fe/ui/src/hooks';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';
import { ServiceMap } from '../components/ServiceMap';
import { ServerMapPage, ServermapPageProps } from './ServerMap';

export type ServiceMapPageProps = Omit<
  ServermapPageProps,
  | 'MapView'
  | 'title'
  | 'helpKey'
  | 'getPagePath'
  | 'getRealtimePagePath'
  | 'requiresApplication'
  | 'serviceName'
>;

export const ServiceMapPage = (props: ServiceMapPageProps) => {
  // 경로에 serviceName이 실려 있으면 그것이, 없으면 전역 선택값이 이 화면의 service다.
  // (`/serviceMap`으로 들어와도 라우트 로더가 곧 serviceName을 붙여 리다이렉트한다.)
  const serviceName = useServiceNameForLink() ?? DEFAULT_SERVICE;
  // DEFAULT service는 고른 application 하나를 기준으로 그리므로 먼저 골라야 하고,
  // 그 외 service는 소속된 모든 application을 모아 그리므로 고를 대상이 없다.
  const isDefaultService = useIsDefaultService();

  // 페이지 안에서의 이동(application 선택, 기간 변경, 조회 옵션 변경)도 serviceName을 유지해야
  // 한다. 빠지면 로더가 전역 선택값으로 다시 붙이는데, 그 값은 다른 탭에서 바뀔 수 있어
  // 보고 있던 service가 갈릴 수 있다.
  const getPagePath = React.useCallback(
    (application?: ApplicationType | null, queryParams?: { [k: string]: string }) =>
      getServiceMapPath(serviceName, application, queryParams),
    [serviceName],
  );

  // 실시간 보기도 같은 service 안에서 이어져야 하므로 serviceName을 실은 servicemap 실시간
  // 경로로 보낸다. (servermap 실시간으로 보내면 map을 그리는 API가 serverMap으로 갈린다.)
  const getRealtimePagePath = React.useCallback(
    (application?: ApplicationType | null) => getServiceMapRealtimePath(serviceName, application),
    [serviceName],
  );

  return (
    <ServerMapPage
      {...props}
      MapView={ServiceMap}
      title="Servicemap"
      helpKey="HELP_VIEWER.SERVICE_MAP"
      getPagePath={getPagePath}
      getRealtimePagePath={getRealtimePagePath}
      requiresApplication={isDefaultService}
      serviceName={serviceName}
    />
  );
};
