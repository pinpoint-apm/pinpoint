import { getRequestService } from '@pinpoint-fe/ui/src/hooks';
import { parseServiceScopedPath, toRouterPath } from '@pinpoint-fe/ui/src/utils';

/**
 * `/{page}/{serviceName}?/{applicationName}@{serviceType}?` 형태의 경로를 분해하고, 이 경로가
 * 실어야 할 serviceName 세그먼트까지 정한다. 사이드 메뉴로 옮겨 다니는 application 단위 화면
 * (inspector, urlStatistic, errorAnalysis, openTelemetryMetric)과 threadDump의 로더가 쓴다.
 *
 * servicemap 로더와 같은 규칙이다 — serviceName 세그먼트가 없으면 지금 보고 있는 service
 * (`getRequestService`)를 붙인 표준 형태를 리다이렉트 목적지로 돌려준다. 세그먼트가 생기기 전
 * 형태의 링크·북마크로 들어온 경우다. (메뉴 링크는 application이 없어도 경로 빌더가 serviceName을
 * 싣는다.) URL에 service가 남아야 새 탭으로 복사해 열어도 같은 service로 조회한다.
 *
 * `enableServiceMap`이 꺼져 있으면 `getRequestService`가 undefined이므로 세그먼트를 붙이지 않는다
 * — 예전과 같은 경로가 된다. 설정을 읽어야 판단할 수 있으므로 **`getConfiguration`을 기다린 뒤에**
 * 호출한다(그 함수가 `configurationAtom`을 채운다).
 *
 * **인코딩된(raw) pathname을 넘긴다.** react-router의 `params`는 디코딩된 값이라 serviceName 안의
 * '%2F'가 '/'로 풀려 세그먼트 경계가 어긋나고, 리다이렉트 목적지에도 원본 '/'가 실린다.
 *
 * `request.url`의 pathname을 그대로 넘기면 된다 — 라우터 basename(`BASE_PATH`)은 여기서 뗀다.
 * 로더의 `request.url`에는 basename이 붙어 있어, 떼지 않고 `pagePath` 길이로 자르면 세그먼트가
 * 어긋난다(`/pinpoint/threadDump/{app}@{type}`에서 application을 못 읽어 servermap으로 쫓겨났다).
 * 리다이렉트 목적지(`basePath`)는 라우터 기준 경로라 basename 없이 만든다(라우터가 붙인다).
 */
export const resolveServiceScopedPage = (pagePath: string, pathname: string) => {
  const { encodedServiceName, applicationSegment, application } = parseServiceScopedPath(
    pagePath,
    toRouterPath(pathname),
  );
  const requestService = encodedServiceName ? undefined : getRequestService();
  const serviceSegment =
    encodedServiceName ?? (requestService ? encodeURIComponent(requestService) : undefined);
  const hasApplication = !!(application?.applicationName && application.serviceType);

  return {
    application,
    /** 리다이렉트 목적지로 쓸, serviceName 세그먼트까지 갖춘 기준 경로(query string 제외). */
    basePath: `${pagePath}${serviceSegment ? `/${serviceSegment}` : ''}${
      hasApplication ? `/${applicationSegment}` : ''
    }`,
    /** 경로에 serviceName이 없어서 `basePath`로 옮겨야 하는지. */
    isServiceNameMissing: !encodedServiceName && !!serviceSegment,
  };
};
