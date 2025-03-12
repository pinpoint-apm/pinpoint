import {
  getLayoutWithConfiguration,
  getLayoutWithSideNavigation,
} from '@pinpoint-fe/web/src/components/Layout';
import { AgentManagementPage, withInitialFetch } from '@pinpoint-fe/ui';

export default withInitialFetch((props) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<AgentManagementPage {...props} />)),
);
