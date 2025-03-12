import { getLayoutWithSideNavigation } from '@pinpoint-fe/web/src/components/Layout/LayoutWithSideNavigation';
import { withInitialFetch, RealtimePage } from '@pinpoint-fe/ui';

export default withInitialFetch((props) =>
  getLayoutWithSideNavigation(<RealtimePage {...props} />),
);
