import { useSearchParameters } from '@pinpoint-fe/ui/hooks';
import { GroupMemberTable } from './GroupMemberTable';

export interface GroupMemberProps {}

export const GroupMember = () => {
  const { searchParameters } = useSearchParameters();
  const userGroupName = searchParameters?.groupName;

  return (
    <>
      <h3 className="text-base font-semibold">{userGroupName}</h3>
      <GroupMemberTable
        userGroupId={userGroupName}
        enableAllGroupMemberAdd={true}
        enableAllGroupMemberRemove={true}
      />
    </>
  );
};
