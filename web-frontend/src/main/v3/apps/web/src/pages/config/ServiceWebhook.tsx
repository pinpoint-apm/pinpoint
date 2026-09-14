import { ServiceWebhookPage } from '@pinpoint-fe/ui';
import { useIsDefaultService } from '@pinpoint-fe/ui/src/hooks';
import Webhook from './Webhook';

export default function ServiceWebhook() {
  const isDefaultService = useIsDefaultService();

  // DEFAULT service는 service 단위 webhook이 없으므로 application 단위 webhook 화면을 그대로 쓴다.
  // (servicemap이 켜지면 `/config/webhook`은 이 경로로 옮겨진다 — `HIDDEN_PAGE_RULES`의 Webhook 쌍.)
  if (isDefaultService) {
    return <Webhook />;
  }

  return <ServiceWebhookPage />;
}
