import { useAtomValue } from 'jotai';
import { InspectorPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function Inspector() {
  const configuration = useAtomValue(configurationAtom);
  return <InspectorPage configuration={configuration} />;
}
