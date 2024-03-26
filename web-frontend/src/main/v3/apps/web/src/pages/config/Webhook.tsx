import { WebhookPage, WebhookPageProps, withInitialFetch } from '@pinpoint-fe/ui';
import { getLayoutWithConfiguration, getLayoutWithSideNavigation } from '@/components/Layout';

export default withInitialFetch((props: WebhookPageProps) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<WebhookPage {...props} />)),
);
