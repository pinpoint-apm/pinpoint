import { useAtomValue } from 'jotai';
import { WebhookPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function Webhook() {
  const configuration = useAtomValue(configurationAtom);
  return <WebhookPage configuration={configuration} />;
}
