import { useAtomValue } from 'jotai';
import { ScatterFullScreenPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export default function ScatterFullScreen() {
  const configuration = useAtomValue(configurationAtom) as
    | (Configuration & Record<string, unknown>)
    | undefined;
  return <ScatterFullScreenPage configuration={configuration} />;
}
