import { useAtomValue } from 'jotai';
import { AgentManagementPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function AgentManagement() {
  const configuration = useAtomValue(configurationAtom);
  return <AgentManagementPage configuration={configuration} />;
}
