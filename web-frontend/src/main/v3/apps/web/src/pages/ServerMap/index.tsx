import { getLayoutWithSideNavigation } from '@pinpoint-fe/web/src/components/Layout/LayoutWithSideNavigation';
import { withInitialFetch, ServerMapPage } from '@pinpoint-fe/ui';

export default withInitialFetch((props) =>
  getLayoutWithSideNavigation(<ServerMapPage {...props} />),
);
