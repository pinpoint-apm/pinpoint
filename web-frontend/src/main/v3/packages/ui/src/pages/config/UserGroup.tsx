import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { useSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { MdArrowForwardIos } from 'react-icons/md';
import { Link } from 'react-router';
import { UserGroup, GroupMember } from '../../components/Config';

export const UserGroupPage = () => {
  const { searchParameters } = useSearchParameters();
  const userGroupName = searchParameters?.groupName;

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">
          {userGroupName ? (
            <div className="flex items-center gap-1">
              <Link
                className="text-muted-foreground hover:underline"
                to={APP_PATH.CONFIG_USER_GROUP}
              >
                User Group
              </Link>
              <MdArrowForwardIos className="text-sm text-muted-foreground" />
              {userGroupName}
            </div>
          ) : (
            <>User Group</>
          )}
        </h3>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-[1px] w-full"
      ></div>
      {userGroupName ? <GroupMember /> : <UserGroup />}
    </div>
  );
};
