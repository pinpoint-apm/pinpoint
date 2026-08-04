import { useConfiguration } from '@pinpoint-fe/ui/src/hooks';
import { UserGroupTable } from './UserGroupTable';

export const UserGroup = () => {
  const configuration = useConfiguration();

  return (
    configuration && <UserGroupTable enableUserGroupAdd={true} enableAllUserGroupRemove={true} />
  );
};
