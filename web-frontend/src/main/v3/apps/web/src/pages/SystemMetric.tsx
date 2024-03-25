import { withInitialFetch, SystemMetricPage } from '@pinpoint-fe/ui';
import { getLayoutWithSideNavigation } from '@/components/Layout/LayoutWithSideNavigation';

export default withInitialFetch(() => getLayoutWithSideNavigation(<SystemMetricPage />));
