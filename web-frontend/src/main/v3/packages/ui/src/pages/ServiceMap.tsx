import { ServiceMap } from '../components/ServiceMap';
import { ServerMapPage, ServermapPageProps } from './ServerMap';

export type ServiceMapPageProps = Omit<ServermapPageProps, 'MapView' | 'title'>;

export const ServiceMapPage = (props: ServiceMapPageProps) => {
  return <ServerMapPage {...props} MapView={ServiceMap} title="Servicemap" />;
};
