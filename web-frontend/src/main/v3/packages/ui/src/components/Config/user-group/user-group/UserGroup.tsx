import { Configuration } from '@pinpoint-fe/constants';
import { UserGroupTable } from './UserGroupTable';

export interface UserGroupProps {
  configuration?: Configuration;
}

export const UserGroup = ({ configuration }: UserGroupProps) => {
  return (
    configuration && <UserGroupTable enableUserGroupAdd={true} enableAllUserGroupRemove={true} />
  );
};
