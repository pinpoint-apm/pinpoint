import { TransactionListPage, TransactionListPageProps, withInitialFetch } from '@pinpoint-fe/ui';
import { getLayoutWithSideNavigation } from '@pinpoint-fe/web/src/components/Layout/LayoutWithSideNavigation';

export default withInitialFetch((props: TransactionListPageProps) =>
  getLayoutWithSideNavigation(<TransactionListPage {...props} />),
);
