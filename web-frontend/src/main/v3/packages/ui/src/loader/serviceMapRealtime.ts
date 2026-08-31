import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import { getRequestService } from '@pinpoint-fe/ui/src/hooks';
import { getServiceMapRealtimePath, parseServiceScopedPath } from '@pinpoint-fe/ui/src/utils';
import { LoaderFunctionArgs, redirect } from 'react-router';

/**
 * servicemap 실시간 보기의 라우트 로더.
 *
 * 표준 형태는 `/serviceMap/realtime/{serviceName}/{application}@{serviceType}?` 하나뿐이다.
 * 실시간 보기는 기간을 "지금부터 5분 전"으로 화면이 직접 만들기 때문에 query string이 없다.
 *
 * 1) serviceName 세그먼트가 없으면 현재 보고 있는 service를 붙인다.
 * 2) DEFAULT가 아닌 service는 소속 application을 모두 모아 map을 그리므로 기준 application이
 *    없다. 다른 화면에서 링크를 타고 실려 들어온 application 세그먼트를 그대로 두면, 클릭한
 *    노드가 아니라 그 application의 수치를 우측 패널에 보여주게 되므로 지운다.
 *    (DEFAULT는 고를 대상이 있으므로, 아직 고르지 않았으면 화면이 선택 박스를 띄운다.)
 * 3) 실려 들어온 query string은 지운다. (servermap 실시간 보기와 같은 처리)
 *
 * 세 가지를 따로 판단해 각각 리다이렉트하지 않고 목적지를 한 번에 정한다. serviceName이 없고
 * 현재 service가 DEFAULT도 아닌 옛 링크는 둘 다 걸리는데, 나눠 두면 service를 붙인 경로로 갔다가
 * application을 떼려고 또 한 번 움직인다.
 */
export const serviceMapRealtimeLoader = ({ request }: LoaderFunctionArgs) => {
  try {
    const { pathname, search } = new URL(request.url);
    const { serviceName, application } = parseServiceScopedPath(
      APP_PATH.SERVICE_MAP_REALTIME,
      pathname,
    );

    const resolvedServiceName = serviceName || getRequestService();
    const resolvedApplication = resolvedServiceName === DEFAULT_SERVICE ? application : null;
    const hasStaleApplication = !!application && !resolvedApplication;

    if (!serviceName || hasStaleApplication || search) {
      return redirect(getServiceMapRealtimePath(resolvedServiceName, resolvedApplication));
    }

    return application;
  } catch (err) {
    console.error('Error in serviceMapRealtimeLoader:', err);
    return null;
  }
};
