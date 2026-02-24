import { useAtomValue } from 'jotai';
import { UsersPage as CommonUsersPage } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export default function Users() {
  const configuration = useAtomValue(configurationAtom);
  return <CommonUsersPage configuration={configuration} />;
}
