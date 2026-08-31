import {
  APP_PATH,
  Configuration,
  SEARCH_PARAMETER_DATE_FORMAT,
} from '@pinpoint-fe/ui/src/constants';
import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import {
  getApplicationTypeAndName,
  getParsedDateRange,
  getServiceNameFromPath,
  isValidDateRange,
  getTimezone,
} from '@pinpoint-fe/ui/src/utils';
import { parse } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { LoaderFunctionArgs, redirect } from 'react-router';

export const transactionRouteLoader = async ({ request }: LoaderFunctionArgs) => {
  const requestUrl = new URL(request.url);
  // 경로는 `/transactionList/{serviceName}/{applicationName}@{serviceType}` 형태다.
  //
  // react-router의 `params`는 디코딩된 값이라 그대로 쓸 수 없다. serviceName에 인코딩된 '/'가
  // 있으면 '%2F'가 '/'로 풀려 세그먼트 경계가 어긋나고, 아래 리다이렉트 경로에도 원본 '/'가
  // 실려 라우트 매칭이 깨진다. 그래서 파싱과 리다이렉트 모두 인코딩된 원본 경로를 쓴다.
  const [firstSegment, secondSegment] = requestUrl.pathname
    .slice(APP_PATH.TRANSACTION_LIST.length)
    .replace(/^\//, '')
    .split('/');
  const serviceName = getServiceNameFromPath(requestUrl.pathname);
  // 리다이렉트 목적지에 다시 실을, 인코딩된 그대로의 serviceName 세그먼트.
  const encodedServiceName = serviceName ? firstSegment : undefined;
  const applicationSegment = encodedServiceName ? secondSegment : firstSegment;
  const parsedApplication = getApplicationTypeAndName(applicationSegment);
  // application을 못 읽으면 null을 그대로 돌려준다(기존 계약).
  const application = parsedApplication && { serviceName, ...parsedApplication };

  let configuration: Configuration | undefined;
  try {
    configuration = await getConfiguration<Configuration>();
  } catch {
    // Continue with defaults so that date params are still redirected.
  }

  const timezone = getTimezone();

  if (application?.applicationName && application.serviceType) {
    const basePath = `${APP_PATH.TRANSACTION_LIST}${
      encodedServiceName ? `/${encodedServiceName}` : ''
    }/${applicationSegment}`;
    const queryParam = Object.fromEntries(requestUrl.searchParams);
    const conditions = Object.keys(queryParam);

    const from = queryParam?.from as string;
    const to = queryParam?.to as string;

    const currentDate = new Date();
    const parsedDateRange = {
      from: parse(from, SEARCH_PARAMETER_DATE_FORMAT, currentDate),
      to: parse(to, SEARCH_PARAMETER_DATE_FORMAT, currentDate),
    };
    const validateDateRange = isValidDateRange(configuration?.['periodMax.serverMap'] || 2);
    const defaultParsedDateRange = getParsedDateRange({ from, to }, validateDateRange);
    const defaultFormattedDateRange = {
      from: formatInTimeZone(defaultParsedDateRange.from, timezone, SEARCH_PARAMETER_DATE_FORMAT),
      to: formatInTimeZone(defaultParsedDateRange.to, timezone, SEARCH_PARAMETER_DATE_FORMAT),
    };
    const defaultDatesQueryString = new URLSearchParams(defaultFormattedDateRange).toString();
    const defaultDestination = `${basePath}?${defaultDatesQueryString}`;

    if (conditions.length === 0) {
      return redirect(defaultDestination);
    } else {
      if (
        conditions.includes('from') &&
        conditions.includes('to') &&
        validateDateRange(parsedDateRange)
      ) {
        return application;
      } else {
        return redirect(defaultDestination);
      }
    }
  }

  return application;
};
