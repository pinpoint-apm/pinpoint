import { useAtomValue } from 'jotai';
import { TransactionDetailPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export default function TransactionDetail() {
  const configuration = useAtomValue(configurationAtom) as
    | (Configuration & Record<string, string>)
    | undefined;
  return <TransactionDetailPage configuration={configuration} />;
}
