import { useAtomValue } from 'jotai';
import { ServerMapPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { APP_PATH, Configuration } from '@pinpoint-fe/ui/src/constants';
import { Navigate, useLocation, useParams } from 'react-router-dom';

export default function ServerMap() {
  const configuration = useAtomValue(configurationAtom) as
    | (Configuration & Record<string, string>)
    | undefined;
  const { application } = useParams<{ application?: string }>();
  const { search } = useLocation();

  if (configuration?.['experimental.enableServiceMap.value']) {
    const applicationPath = application ? `/${application}` : '';
    return <Navigate to={`${APP_PATH.SERVICE_MAP}${applicationPath}${search}`} replace />;
  }

  return <ServerMapPage configuration={configuration} />;
}
