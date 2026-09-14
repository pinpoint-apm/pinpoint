import { ServiceAlarmPage } from '@pinpoint-fe/ui';
import { useIsDefaultService } from '@pinpoint-fe/ui/src/hooks';
import Alarm from './Alarm';

export default function ServiceAlarm() {
  const isDefaultService = useIsDefaultService();

  // DEFAULT service는 service 단위 alarm이 없으므로 application 단위 alarm 화면을 그대로 쓴다.
  // (servicemap이 켜지면 `/config/alarm`은 이 경로로 옮겨진다 — `HIDDEN_PAGE_RULES`의 Alarm 쌍.)
  if (isDefaultService) {
    return <Alarm />;
  }

  return <ServiceAlarmPage />;
}
