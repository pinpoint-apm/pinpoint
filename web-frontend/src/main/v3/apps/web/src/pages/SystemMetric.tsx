import { useAtomValue } from 'jotai';
import { SystemMetricPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export default function SystemMetric() {
  const configuration = useAtomValue(configurationAtom) as
    | (Configuration & Record<string, unknown>)
    | undefined;
  return <SystemMetricPage configuration={configuration} />;
}
