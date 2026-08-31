import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { parseServiceScopedPath } from '@pinpoint-fe/ui/src/utils';
import { LoaderFunctionArgs, redirect } from 'react-router';
import { resolveMapDateRangeRedirect } from './mapDateRange';

/**
 * filteredMap 페이지의 라우트 로더.
 *
 * 경로는 두 형태를 받는다.
 * - `/filteredMap/{serviceName}/{application}@{serviceType}` — servicemap에서 넘어온 형태.
 *   serviceName이 실려 있어야 이 화면의 모든 조회가 그 service로 나간다(URL이 진실의 원천).
 *   filteredMap은 새 탭으로 열리므로 전역 선택값은 믿을 수 없다. 다른 탭에서 service를 바꾸면
 *   이 탭의 조회 대상이 함께 갈린다.
 * - `/filteredMap/{application}@{serviceType}` — servermap에서 넘어온 형태(그리고 옛 링크·북마크).
 *   serviceName이 없으므로 조회는 전역 선택값으로 폴백한다.
 *
 * **없는 serviceName 세그먼트를 채워 넣지 않는다.** 두 형태의 구분이 곧 "어느 map에서 왔는가"라서
 * (화면이 그것으로 돌아갈 map을 정한다) 채워 넣으면 servermap에서 온 화면이 servicemap에서 온
 * 것처럼 보인다. servicemap이 servermap을 대체해 링크가 항상 serviceName을 싣게 되면 이 폴백
 * 형태는 옛 링크만 남아 사라진다.
 *
 * 그 외에는 map 계열의 공통 처리인 날짜 정규화만 한다. application이 없으면 그릴 map이 없으므로
 * 날짜도 채우지 않는다.
 */
export const filteredMapRouteLoader = async ({ request }: LoaderFunctionArgs) => {
  const { pathname } = new URL(request.url);
  const { encodedServiceName, applicationSegment, application } = parseServiceScopedPath(
    APP_PATH.FILTERED_MAP,
    pathname,
  );

  if (!application?.applicationName || !application.serviceType) {
    return application;
  }

  const destination = await resolveMapDateRangeRedirect({
    requestUrl: request.url,
    // 리다이렉트가 경로 형태를 바꾸면 안 된다. serviceName이 실려 있으면 그대로 유지한다.
    // (예전에는 servermap 로더를 그대로 써서, 날짜를 고치는 리다이렉트가 filteredMap을
    //  `/serverMap/...`으로 보내며 filter까지 떨어뜨렸다.)
    // serviceName은 인코딩된 세그먼트를 그대로 쓴다. 디코딩해서 다시 인코딩하면 사용자가 직접
    // 편집한 잘못된 인코딩('%' 하나)에서 던진다.
    basePath: `${APP_PATH.FILTERED_MAP}${
      encodedServiceName ? `/${encodedServiceName}` : ''
    }/${applicationSegment}`,
  });

  if (destination) {
    return redirect(destination);
  }

  return application;
};
