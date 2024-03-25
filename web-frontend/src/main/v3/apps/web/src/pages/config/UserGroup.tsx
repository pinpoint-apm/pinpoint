import { getLayoutWithConfiguration, getLayoutWithSideNavigation } from '@/components/Layout';
import { useAtomValue } from 'jotai';
import { Link } from 'react-router-dom';
import { APP_PATH } from '@pinpoint-fe/constants';
import { MdArrowForwardIos } from 'react-icons/md';
import { useSearchParameters } from '@pinpoint-fe/hooks';
import { GroupMember, UserGroup, withInitialFetch } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/atoms';

export interface UserGroupPageProps {}
const UserGroupPage = () => {
  const configuration = useAtomValue(configurationAtom);
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
              Group Members
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
      {userGroupName ? <GroupMember /> : <UserGroup configuration={configuration} />}
    </div>
  );
};

export default withInitialFetch((props: UserGroupPageProps) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<UserGroupPage {...props} />)),
);
