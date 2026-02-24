import { useAtomValue } from 'jotai';
import { UserGroupPage as CommonUserGroupPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function UserGroup() {
  const configuration = useAtomValue(configurationAtom);
  return <CommonUserGroupPage configuration={configuration} />;
}
