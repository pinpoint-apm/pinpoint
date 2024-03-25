import { GeneralPage, withInitialFetch } from '@pinpoint-fe/ui';
import { getLayoutWithConfiguration, getLayoutWithSideNavigation } from '@/components/Layout';

export default withInitialFetch(() =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<GeneralPage />)),
);
