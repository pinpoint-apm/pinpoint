import { getLayoutWithConfiguration, getLayoutWithSideNavigation } from '@/components/Layout';
import { useAtomValue } from 'jotai';
import { Users, withInitialFetch } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/atoms';

export interface UsersPageProps {}
const UsersPage = () => {
  const configuration = useAtomValue(configurationAtom);

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Users</h3>
      </div>
      <div
        data-orientation="horizontal"
        role="none"
        className="shrink-0 bg-border h-[1px] w-full"
      ></div>
      <Users autoResize configuration={configuration} />
    </div>
  );
};

export default withInitialFetch((props: UsersPageProps) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<UsersPage {...props} />)),
);
