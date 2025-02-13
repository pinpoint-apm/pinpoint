import { useAtomValue } from 'jotai';
import {
  getLayoutWithConfiguration,
  getLayoutWithSideNavigation,
} from '@pinpoint-fe/web/src/components/Layout';
import { UsersPage as CommonUsersPage, withInitialFetch } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export interface UsersPageProps {}
const UsersPage = () => {
  const configuration = useAtomValue(configurationAtom);

  return <CommonUsersPage configuration={configuration} />;
};

export default withInitialFetch((props: UsersPageProps) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<UsersPage {...props} />)),
);
