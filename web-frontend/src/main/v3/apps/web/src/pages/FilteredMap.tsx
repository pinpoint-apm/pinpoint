import { getLayoutWithSideNavigation } from '@pinpoint-fe/web/src/components/Layout/LayoutWithSideNavigation';
import { withInitialFetch, FilteredMapPage } from '@pinpoint-fe/ui';

export default withInitialFetch((props) =>
  getLayoutWithSideNavigation(<FilteredMapPage {...props} />),
);
