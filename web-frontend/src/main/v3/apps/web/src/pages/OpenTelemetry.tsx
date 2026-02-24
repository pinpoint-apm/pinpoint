import { useAtomValue } from 'jotai';
import { OpenTelemetryPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export default function OpenTelemetry() {
  const configuration = useAtomValue(configurationAtom) as
    | (Configuration & Record<string, unknown>)
    | undefined;
  return <OpenTelemetryPage configuration={configuration} />;
}
