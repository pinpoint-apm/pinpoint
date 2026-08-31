import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import { getRequestService } from '@pinpoint-fe/ui/src/hooks';
import { getServiceMapPath, parseServiceScopedPath } from '@pinpoint-fe/ui/src/utils';
import { LoaderFunctionArgs, redirect } from 'react-router';
import { resolveMapDateRangeRedirect } from './mapDateRange';

/**
 * servicemap 페이지의 라우트 로더.
 *
 * 이 화면은 "어떤 service를 보는 중인지"가 URL의 진실의 원천이다. 그래서 두 가지를 보장한다.
 *
 * 1) serviceName 세그먼트가 없으면 현재 보고 있는 service를 붙여 표준 형태로 옮긴다.
 *    (`/serviceMap`으로 직접 들어온 경우, 그리고 serviceName이 없던 옛 링크)
 * 2) map을 그릴 수 있는 상태면 from/to를 표준 형식(`SEARCH_PARAMETER_DATE_FORMAT`)으로 맞춘다.
 *    DEFAULT service는 application을 골라야 그리므로 application이 있을 때만, 그 외 service는
 *    소속 application을 모두 모아 그리므로 언제나 맞춘다. (화면의 `useIsDefaultService`와 같은 규칙)
 */
export const serviceMapRouteLoader = async ({ request }: LoaderFunctionArgs) => {
  const { pathname, search } = new URL(request.url);
  const { serviceName, encodedServiceName, applicationSegment, application } =
    parseServiceScopedPath(APP_PATH.SERVICE_MAP, pathname);
  const hasApplication = !!(application?.applicationName && application.serviceType);

  if (!serviceName || !encodedServiceName) {
    return redirect(`${getServiceMapPath(getRequestService(), application)}${search}`);
  }

  // DEFAULT는 고른 application 하나를 기준으로 그리므로, 고르기 전에는 채울 필요가 없다.
  if (!hasApplication && serviceName === DEFAULT_SERVICE) {
    return application;
  }

  const destination = await resolveMapDateRangeRedirect({
    requestUrl: request.url,
    basePath: `${APP_PATH.SERVICE_MAP}/${encodedServiceName}${
      hasApplication ? `/${applicationSegment}` : ''
    }`,
  });

  if (destination) {
    return redirect(destination);
  }

  return application;
};
