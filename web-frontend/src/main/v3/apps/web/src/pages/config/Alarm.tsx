import { useAtomValue } from 'jotai';
import { AlarmPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function Alarm() {
  const configuration = useAtomValue(configurationAtom);
  return <AlarmPage configuration={configuration} />;
}
