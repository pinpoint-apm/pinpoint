import { getLayoutWithSideNavigation } from '@pinpoint-fe/web/src/components/Layout/LayoutWithSideNavigation';
import { withInitialFetch, InspectorPage } from '@pinpoint-fe/ui';

export default withInitialFetch(() => getLayoutWithSideNavigation(<InspectorPage />));
