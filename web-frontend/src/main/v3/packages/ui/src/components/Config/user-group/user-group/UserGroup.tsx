import { Configuration } from '@pinpoint-fe/ui/src/constants';
import { UserGroupTable } from './UserGroupTable';

export interface UserGroupProps {
  configuration?: Configuration;
}

export const UserGroup = ({ configuration }: UserGroupProps) => {
  return (
    configuration && <UserGroupTable enableUserGroupAdd={true} enableAllUserGroupRemove={true} />
  );
};
