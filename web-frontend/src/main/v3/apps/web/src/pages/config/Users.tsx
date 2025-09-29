import { useAtomValue } from 'jotai';
import {
  getLayoutWithConfiguration,
  getLayoutWithSideNavigation,
} from '@pinpoint-fe/web/src/components/Layout';
import { UsersPage as CommonUsersPage, withInitialFetch } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

const UsersPage = () => {
  const configuration = useAtomValue(configurationAtom);

  return <CommonUsersPage configuration={configuration} />;
};

export default withInitialFetch((props) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<UsersPage {...props} />)),
);
