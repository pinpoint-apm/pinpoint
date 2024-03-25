import { ScatterFullScreenPage, withInitialFetch } from '@pinpoint-fe/ui';
import { getLayoutWithSideNavigation } from '@/components/Layout/LayoutWithSideNavigation';

export default withInitialFetch(() => getLayoutWithSideNavigation(<ScatterFullScreenPage />));
