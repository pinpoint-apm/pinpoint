import { useAtomValue } from 'jotai';
import { ExperimentalPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function Experimentals() {
  const configuration = useAtomValue(configurationAtom);
  return <ExperimentalPage configuration={configuration} />;
}
