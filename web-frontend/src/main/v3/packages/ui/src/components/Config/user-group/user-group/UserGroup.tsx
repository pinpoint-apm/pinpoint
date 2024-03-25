import { Configuration } from '@pinpoint-fe/constants';
import { UserGroupTable } from './UserGroupTable';

export interface UserGroupProps {
  configuration?: Configuration & { userId?: string };
}

export const UserGroup = ({ configuration }: UserGroupProps) => {
  const userId = configuration?.userId || '';

  return (
    configuration && (
      <UserGroupTable userId={userId} enableUserGroupAdd={true} enableAllUserGroupRemove={true} />
    )
  );
};
