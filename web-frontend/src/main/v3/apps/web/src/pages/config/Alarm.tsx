import { AlarmPage, AlarmPageProps, withInitialFetch } from '@pinpoint-fe/ui';
import { getLayoutWithConfiguration, getLayoutWithSideNavigation } from '@/components/Layout';

export default withInitialFetch((props: AlarmPageProps) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<AlarmPage {...props} />)),
);
