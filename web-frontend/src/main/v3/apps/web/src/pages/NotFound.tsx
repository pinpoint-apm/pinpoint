import { NotFound404, withInitialFetch } from '@pinpoint-fe/ui';
import { getLayoutWithSideNavigation } from '@/components/Layout/LayoutWithSideNavigation';

export default withInitialFetch(() => getLayoutWithSideNavigation(<NotFound404 />));
