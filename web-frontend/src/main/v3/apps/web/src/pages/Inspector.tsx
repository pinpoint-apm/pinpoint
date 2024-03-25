import { getLayoutWithSideNavigation } from '@/components/Layout/LayoutWithSideNavigation';
import { withInitialFetch, InspectorPage } from '@pinpoint-fe/ui';

export default withInitialFetch(() => getLayoutWithSideNavigation(<InspectorPage />));
