import { ServiceWebhookPage, WebhookPage } from '@pinpoint-fe/ui';
import { useIsDefaultService } from '@pinpoint-fe/ui/src/hooks';

export default function Webhook() {
  const isDefaultService = useIsDefaultService();

  // Alarms와 같은 규칙이다 — DEFAULT가 아닌 service는 아직 service 단위 webhook이 없다.
  // `enableServiceMap`이 꺼져 있으면 백엔드가 모든 요청을 DEFAULT로 해석하므로 이 훅은
  // 언제나 true다 — 설정 플래그를 따로 읽지 않아도 된다.
  if (!isDefaultService) {
    return <ServiceWebhookPage />;
  }

  return <WebhookPage />;
}
