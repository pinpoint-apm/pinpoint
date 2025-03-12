import { ExperimentalPage as Experimental, withInitialFetch } from '@pinpoint-fe/ui';
import {
  getLayoutWithConfiguration,
  getLayoutWithSideNavigation,
} from '@pinpoint-fe/web/src/components/Layout';

export default withInitialFetch((props) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<Experimental {...props} />)),
);
