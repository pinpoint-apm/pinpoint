import { useAtomValue } from 'jotai';
import { RealtimePage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export default function Realtime() {
  const configuration = useAtomValue(configurationAtom) as
    | (Configuration & Record<string, string>)
    | undefined;
  return <RealtimePage configuration={configuration} />;
}
