import { withInitialFetch, ErrorAnalysisPage } from '@pinpoint-fe/ui';

import { getLayoutWithSideNavigation } from '@/components/Layout/LayoutWithSideNavigation';

export default withInitialFetch(() => getLayoutWithSideNavigation(<ErrorAnalysisPage />));
