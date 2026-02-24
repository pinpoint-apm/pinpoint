import { useAtomValue } from 'jotai';
import { UrlStatisticPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export default function UrlStatistic() {
  const configuration = useAtomValue(configurationAtom) as
    | (Configuration & Record<string, unknown>)
    | undefined;
  return <UrlStatisticPage configuration={configuration} />;
}
