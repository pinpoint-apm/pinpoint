import { SEARCH_PARAMETER_DATE_FORMAT, Configuration } from '@pinpoint-fe/ui/src/constants';
import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import {
  getParsedDateRange,
  getServiceNameSegmentPage,
  isValidDateRange,
  getTimezone,
  parseServiceScopedPath,
} from '@pinpoint-fe/ui/src/utils';
import { parse } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { LoaderFunctionArgs, redirect } from 'react-router';

/**
 * `/{page}/{serviceName}?/{applicationName}@{serviceType}` 형태의 fullScreenMode 경로를 분해한다.
 *
 * 이 로더들은 scatter/heatmap, 실시간/비실시간 네 라우트가 공유하므로 페이지 접두사를 경로에서
 * 되찾는다. 판정은 serviceName을 읽는 규칙과 같은 함수(`getServiceNameSegmentPage`)에 맡긴다 —
 * 앞 세그먼트만 잘라 쓰면 실시간 경로에서 `/realtime`이 떨어져 리다이렉트가 비실시간 화면으로
 * 나간다.
 *
 * **react-router의 `params`가 아니라 인코딩된 원본 경로에서 읽는다.** `params`는 디코딩된 값이라
 * serviceName 안의 '%2F'가 '/'로 풀려 세그먼트 경계가 어긋나고, 리다이렉트 목적지에도 원본 '/'가
 * 실려 라우트 매칭이 깨진다.
 */
const parseFullScreenPath = (pathname: string) => {
  const pagePath = getServiceNameSegmentPage(pathname);

  if (!pagePath) {
    return null;
  }

  const { encodedServiceName, applicationSegment, application } = parseServiceScopedPath(
    pagePath,
    pathname,
  );

  return {
    application,
    /** 리다이렉트 목적지로 쓸, 지금 경로와 같은 형태의 기준 경로. */
    basePath: `${pagePath}${
      encodedServiceName ? `/${encodedServiceName}` : ''
    }/${applicationSegment}`,
  };
};

export const scatterOrHeatmapFullScreenLoader = async ({ request }: LoaderFunctionArgs) => {
  const url = new URL(request.url);
  const parsed = parseFullScreenPath(url.pathname);
  const application = parsed?.application;

  let configuration: Configuration | undefined;
  try {
    configuration = await getConfiguration<Configuration>();
  } catch {
    // Continue with defaults so that date params are still redirected.
  }

  const timezone = getTimezone();

  if (parsed && application?.applicationName && application.serviceType) {
    const basePath = parsed.basePath;
    const queryParam = Object.fromEntries(url?.searchParams);
    const conditions = Object.keys(queryParam);

    const from = queryParam?.from ?? '';
    const to = queryParam?.to ?? '';

    const currentDate = new Date();
    const parsedDateRange = {
      from: parse(from, SEARCH_PARAMETER_DATE_FORMAT, currentDate),
      to: parse(to, SEARCH_PARAMETER_DATE_FORMAT, currentDate),
    };
    const defaultParsedDateRange = getParsedDateRange({ from, to });
    const defaultFormattedDateRange = {
      from: formatInTimeZone(defaultParsedDateRange.from, timezone, SEARCH_PARAMETER_DATE_FORMAT),
      to: formatInTimeZone(defaultParsedDateRange.to, timezone, SEARCH_PARAMETER_DATE_FORMAT),
    };
    const defaultDatesQueryString = new URLSearchParams(defaultFormattedDateRange).toString();
    const defaultDestination = `${basePath}?${defaultDatesQueryString}`;

    if (conditions.length === 0) {
      return redirect(defaultDestination);
    } else if (conditions.includes('from')) {
      if (
        conditions.includes('to') &&
        isValidDateRange(configuration?.['periodMax.serverMap'] || 2)(parsedDateRange)
      ) {
        return application;
      } else {
        return redirect(defaultDestination);
      }
    }
  } else {
    return redirect('/');
  }

  return application;
};

export const scatterOrHeatmapFullScreenRealtimeLoader = ({ request }: LoaderFunctionArgs) => {
  try {
    const url = new URL(request.url);
    const parsed = parseFullScreenPath(url.pathname);
    const application = parsed?.application;
    const queryParam = Object.fromEntries(url?.searchParams);
    const queryParamKeys = Object.keys(queryParam);

    if (parsed && queryParamKeys.filter((key) => key !== 'agentId').length > 0) {
      return redirect(`${parsed.basePath}?agentId=${queryParam.agentId}`);
    }

    return application;
  } catch (err) {
    console.error('Error in scatterOrHeatmapFullScreenRealtimeLoader:', err);
    return null;
  }
};
