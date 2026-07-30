import { getServiceMapPath } from '@pinpoint-fe/ui/src/utils';
import { ServiceMap } from '../components/ServiceMap';
import { ServerMapPage, ServermapPageProps } from './ServerMap';

export type ServiceMapPageProps = Omit<ServermapPageProps, 'MapView' | 'title' | 'getPagePath'>;

export const ServiceMapPage = (props: ServiceMapPageProps) => {
  return (
    <ServerMapPage
      {...props}
      MapView={ServiceMap}
      title="Servicemap"
      getPagePath={getServiceMapPath}
    />
  );
};
