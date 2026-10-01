import {
  APP_PATH,
  Configuration,
  SEARCH_PARAMETER_DATE_FORMAT,
} from '@pinpoint-fe/ui/src/constants';
import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import { getParsedDateRange, isValidDateRange, getTimezone } from '@pinpoint-fe/ui/src/utils';
import { parse } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { LoaderFunctionArgs, redirect } from 'react-router';
import { resolveServiceScopedPage } from './serviceScopedPage';

export const urlStatisticRouteLoader = async ({ request }: LoaderFunctionArgs) => {
  const requestUrl = new URL(request.url);

  let configuration: Configuration | undefined;
  try {
    configuration = await getConfiguration<Configuration>();
  } catch {
    // Continue with defaults so that date params are still redirected.
  }

  const timezone = getTimezone();
  // 설정을 읽은 뒤에 불러야 한다 — serviceName 세그먼트를 붙일지는 enableServiceMap에 달렸다.
  const { application, basePath, isServiceNameMissing } = resolveServiceScopedPage(
    APP_PATH.URL_STATISTIC,
    requestUrl.pathname,
  );

  if (application?.applicationName && application.serviceType) {
    const queryParam = Object.fromEntries(requestUrl.searchParams);
    const conditions = Object.keys(queryParam);

    const from = queryParam?.from ?? '';
    const to = queryParam?.to ?? '';

    const currentDate = new Date();
    const parsedDateRange = {
      from: parse(from, SEARCH_PARAMETER_DATE_FORMAT, currentDate),
      to: parse(to, SEARCH_PARAMETER_DATE_FORMAT, currentDate),
    };
    const validateDateRange = isValidDateRange(configuration?.['periodMax.uriStat'] || 28);
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
        // 날짜는 이미 표준 형태다. serviceName만 채워 그대로 옮긴다.
        return isServiceNameMissing ? redirect(`${basePath}${requestUrl.search}`) : application;
      } else {
        return redirect(defaultDestination);
      }
    }
  }

  if (isServiceNameMissing) {
    return redirect(`${basePath}${requestUrl.search}`);
  }

  return application;
};
