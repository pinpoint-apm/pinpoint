import {
  getLayoutWithConfiguration,
  getLayoutWithSideNavigation,
} from '@pinpoint-fe/web/src/components/Layout';
import { AgentStatisticPage, withInitialFetch } from '@pinpoint-fe/ui';

export default withInitialFetch((props) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<AgentStatisticPage {...props} />)),
);
