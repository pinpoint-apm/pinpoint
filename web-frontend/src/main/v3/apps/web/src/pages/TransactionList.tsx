import { TransactionListPage, TransactionListPageProps, withInitialFetch } from '@pinpoint-fe/ui';
import { getLayoutWithSideNavigation } from '@/components/Layout/LayoutWithSideNavigation';

export default withInitialFetch((props: TransactionListPageProps) =>
  getLayoutWithSideNavigation(<TransactionListPage {...props} />),
);
