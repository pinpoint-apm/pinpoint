import { AlarmPage, AlarmPageProps, withInitialFetch } from '@pinpoint-fe/ui';
import {
  getLayoutWithConfiguration,
  getLayoutWithSideNavigation,
} from '@pinpoint-fe/web/src/components/Layout';

export default withInitialFetch((props: AlarmPageProps) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<AlarmPage {...props} />)),
);
