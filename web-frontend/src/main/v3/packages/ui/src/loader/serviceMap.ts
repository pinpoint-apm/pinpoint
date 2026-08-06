import {
  APP_PATH,
  Configuration,
  SEARCH_PARAMETER_DATE_FORMAT,
  SEARCH_PARAMETER_DATE_FORMAT_WHITE_LIST,
} from '@pinpoint-fe/ui/src/constants';
import { DEFAULT_SERVICE } from '@pinpoint-fe/ui/src/atoms';
import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';
import {
  convertParamsToQueryString,
  getApplicationTypeAndName,
  getFormattedDateRange,
  getParsedDateRange,
  getServiceMapPath,
  getServiceNameFromPath,
  getTimezone,
  isValidDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { parse } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

/**
 * servicemap 경로(`/serviceMap/{serviceName}/{application}?`)를 분해한다.
 *
 * react-router의 `params`는 디코딩된 값이라 serviceName 안의 '%2F'가 '/'로 풀려 세그먼트 경계가
 * 어긋난다. 그래서 인코딩된 원본(raw pathname)에서 직접 읽는다.
 *
 * serviceName이 오기 전에는 이 자리에 application이 있었다(`/serviceMap/{app}@{type}`).
 * 그런 옛 링크·북마크를 새 service로 오해하지 않도록 판정은 화면과 같은 함수
 * (`getServiceNameFromPath`)에 맡긴다. serviceName이 아니라고 판정되면 첫 세그먼트를
 * application으로 읽는다.
 */
const parseServiceMapPath = (pathname: string) => {
  const [firstSegment, secondSegment] = pathname
    .slice(APP_PATH.SERVICE_MAP.length)
    .replace(/^\//, '')
    .split('/');

  const serviceName = getServiceNameFromPath(pathname);
  const applicationSegment = serviceName ? secondSegment : firstSegment;

  return {
    serviceName,
    /** 리다이렉트 목적지를 만들 때 쓰는, 인코딩된 그대로의 serviceName 세그먼트. */
    encodedServiceName: serviceName ? firstSegment : undefined,
    applicationSegment,
    application: getApplicationTypeAndName(applicationSegment),
  };
};

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
    parseServiceMapPath(pathname);
  const hasApplication = !!(application?.applicationName && application.serviceType);

  if (!serviceName || !encodedServiceName) {
    return redirect(`${getServiceMapPath(getRequestService(), application)}${search}`);
  }

  // DEFAULT는 고른 application 하나를 기준으로 그리므로, 고르기 전에는 채울 필요가 없다.
  if (!hasApplication && serviceName === DEFAULT_SERVICE) {
    return application;
  }

  let configuration: Configuration | undefined;
  try {
    configuration = await getConfiguration<Configuration>();
  } catch {
    // Configuration fetch may fail when the backend is down.
    // Continue with defaults so that date params are still redirected.
  }

  const timezone = getTimezone();
  const basePath = `${APP_PATH.SERVICE_MAP}/${encodedServiceName}${
    hasApplication ? `/${applicationSegment}` : ''
  }`;
  const queryParam = Object.fromEntries(new URL(request.url).searchParams);
  const conditions = Object.keys(queryParam);

  const from = queryParam?.from as string;
  const to = queryParam?.to as string;

  const currentDate = new Date();
  const validationRange = isValidDateRange(configuration?.['periodMax.serverMap'] || 2);
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
    if (!conditions.includes('to')) {
      return redirect(defaultDestination);
    }

    const matchedFormat = SEARCH_PARAMETER_DATE_FORMAT_WHITE_LIST.find((dateFormat) => {
      const parsedDateRange = {
        from: parse(from, dateFormat, currentDate),
        to: parse(to, dateFormat, currentDate),
      };

      return validationRange(parsedDateRange);
    });

    if (!matchedFormat) {
      return redirect(defaultDestination);
    }

    if (matchedFormat !== SEARCH_PARAMETER_DATE_FORMAT) {
      const parsedDateRange = {
        from: parse(from, matchedFormat, currentDate),
        to: parse(to, matchedFormat, currentDate),
      };
      const formattedDataRange = getFormattedDateRange(parsedDateRange);
      const destination = `${basePath}?${convertParamsToQueryString({
        ...queryParam,
        ...formattedDataRange,
      })}`;
      return redirect(destination);
    }
  }

  return application;
};
