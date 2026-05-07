import { useAtomValue } from 'jotai';
import { ServerMapPage, ServiceMapPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export default function ServerMap() {
  const configuration = useAtomValue(configurationAtom) as
    | (Configuration & Record<string, string>)
    | undefined;
  const enableServiceMap = !!configuration?.['experimental.enableServiceMap.value'];
  const Page = enableServiceMap ? ServiceMapPage : ServerMapPage;
  return <Page configuration={configuration} />;
}
