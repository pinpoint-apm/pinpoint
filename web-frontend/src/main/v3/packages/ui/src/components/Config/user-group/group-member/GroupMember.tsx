import { useSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { GroupMemberTable } from './GroupMemberTable';

export interface GroupMemberProps {}

export const GroupMember = () => {
  const { searchParameters } = useSearchParameters();
  const userGroupName = searchParameters?.groupName;

  return (
    <>
      <GroupMemberTable
        userGroupId={userGroupName}
        enableAllGroupMemberAdd={true}
        enableAllGroupMemberRemove={true}
      />
    </>
  );
};
