import { useAtomValue } from 'jotai';
import { ServiceSettingPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function ServiceSetting() {
  const configuration = useAtomValue(configurationAtom);
  return <ServiceSettingPage configuration={configuration} />;
}
