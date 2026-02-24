import { useAtomValue } from 'jotai';
import { AgentStatisticPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function AgentStatistic() {
  const configuration = useAtomValue(configurationAtom);
  return <AgentStatisticPage configuration={configuration} />;
}
