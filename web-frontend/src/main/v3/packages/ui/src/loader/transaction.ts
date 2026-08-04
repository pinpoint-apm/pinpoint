import {
  APP_PATH,
  Configuration,
  SEARCH_PARAMETER_DATE_FORMAT,
} from '@pinpoint-fe/ui/src/constants';
import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import {
  getServiceAndApplicationTypeAndName,
  getParsedDateRange,
  isValidDateRange,
  getTimezone,
} from '@pinpoint-fe/ui/src/utils';
import { parse } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const transactionRouteLoader = async ({ params, request }: LoaderFunctionArgs) => {
  const requestUrl = new URL(request.url);
  // 세그먼트가 `{serviceName}@{applicationName}@{serviceType}`일 수 있으므로 serviceName을 분리한다.
  //
  // react-router의 `params`는 디코딩된 값이라 그대로 쓸 수 없다. serviceName에 인코딩된 '/'가
  // 있으면 '%2F'가 '/'로 풀려 세그먼트 경계가 어긋나고, 아래 리다이렉트 경로에도 원본 '/'가
  // 실려 라우트 매칭이 깨진다. 그래서 파싱과 리다이렉트 모두 인코딩된 원본 세그먼트를 쓴다.
  const applicationSegment = params.application
    ? requestUrl.pathname.split('/').pop()
    : params.application;
  const application = getServiceAndApplicationTypeAndName(applicationSegment);

  let configuration: Configuration | undefined;
  try {
    configuration = await getConfiguration<Configuration>();
  } catch {
    // Continue with defaults so that date params are still redirected.
  }

  const timezone = getTimezone();

  if (application?.applicationName && application.serviceType) {
    const basePath = `${APP_PATH.TRANSACTION_LIST}/${applicationSegment}`;
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
