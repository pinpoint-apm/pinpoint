import { useAtomValue } from 'jotai';
import { ServiceMapPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export default function ServiceMap() {
  const configuration = useAtomValue(configurationAtom) as
    | (Configuration & Record<string, string>)
    | undefined;
  return <ServiceMapPage configuration={configuration} />;
}
