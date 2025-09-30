import { useAtomValue } from 'jotai';
import {
  getLayoutWithConfiguration,
  getLayoutWithSideNavigation,
} from '@pinpoint-fe/web/src/components/Layout';
import { UserGroupPage as CommonUserGroupPage, withInitialFetch } from '@pinpoint-fe/ui';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

const UserGroupPage = () => {
  const configuration = useAtomValue(configurationAtom);

  return <CommonUserGroupPage configuration={configuration} />;
};

export default withInitialFetch((props) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<UserGroupPage {...props} />)),
);
