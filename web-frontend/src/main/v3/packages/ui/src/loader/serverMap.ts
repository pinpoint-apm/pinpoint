import {
  APP_PATH,
  Configuration,
  SEARCH_PARAMETER_DATE_FORMAT,
  SEARCH_PARAMETER_DATE_FORMAT_WHITE_LIST,
} from '@pinpoint-fe/ui/src/constants';
import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import {
  convertParamsToQueryString,
  getApplicationTypeAndName,
  getFormattedDateRange,
  getParsedDateRange,
  getTimezone,
  isValidDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { parse } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { LoaderFunctionArgs, redirect } from 'react-router';
import { resolveHiddenMapPageRedirect } from './hiddenMapPage';

/**
 * from/to를 표준 형식(`SEARCH_PARAMETER_DATE_FORMAT`)으로 맞춰야 하는지 판단해, 맞춰야 하면
 * 리다이렉트 목적지를 반환한다. 이미 맞거나 날짜 조건이 실려 오지 않았으면 undefined다.
 *
 * (`mapDateRange.ts`의 `resolveMapDateRangeRedirect`와 내용이 같다. 그쪽은 servermap이 빠진
 *  뒤에도 남는 화면들이 쓰고 이 파일은 파일째로 사라지므로, 일부러 합치지 않는다.)
 */
const resolveServerMapDateRedirect = ({
  basePath,
  requestUrl,
  periodMax,
}: {
  basePath: string;
  requestUrl: string;
  periodMax?: number;
}): string | undefined => {
  const timezone = getTimezone();
  const queryParam = Object.fromEntries(new URL(requestUrl).searchParams);
  const conditions = Object.keys(queryParam);

  const from = queryParam?.from ?? '';
  const to = queryParam?.to ?? '';

  const currentDate = new Date();
  const validationRange = isValidDateRange(periodMax || 2);
  const defaultParsedDateRange = getParsedDateRange({ from, to });
  const defaultFormattedDateRange = {
    from: formatInTimeZone(defaultParsedDateRange.from, timezone, SEARCH_PARAMETER_DATE_FORMAT),
    to: formatInTimeZone(defaultParsedDateRange.to, timezone, SEARCH_PARAMETER_DATE_FORMAT),
  };
  const defaultDatesQueryString = new URLSearchParams(defaultFormattedDateRange).toString();
  const defaultDestination = `${basePath}?${defaultDatesQueryString}`;

  if (conditions.length === 0) {
    return defaultDestination;
  }

  // from 없이 다른 조건만 실려 온 경우는 날짜를 건드리지 않는다.
  if (!conditions.includes('from')) {
    return undefined;
  }

  if (!conditions.includes('to')) {
    return defaultDestination;
  }

  const matchedFormat = SEARCH_PARAMETER_DATE_FORMAT_WHITE_LIST.find((dateFormat) =>
    validationRange({
      from: parse(from, dateFormat, currentDate),
      to: parse(to, dateFormat, currentDate),
    }),
  );

  if (!matchedFormat) {
    return defaultDestination;
  }

  if (matchedFormat === SEARCH_PARAMETER_DATE_FORMAT) {
    return undefined;
  }

  const formattedDataRange = getFormattedDateRange({
    from: parse(from, matchedFormat, currentDate),
    to: parse(to, matchedFormat, currentDate),
  });

  // from/to 외의 query string은 목적지에도 그대로 싣는다.
  return `${basePath}?${convertParamsToQueryString({ ...queryParam, ...formattedDataRange })}`;
};

/**
 * map 페이지의 날짜 파라미터를 검증/정규화하는 공용 로더.
 * 리다이렉트는 현재 페이지 경로를 유지해야 하므로 pagePath를 받아 로더를 만든다.
 */
export const createMapRouteLoader =
  (pagePath: string) =>
  async ({ params, request }: LoaderFunctionArgs) => {
    // servicemap이 켜져 있으면 servermap은 메뉴에서 감춘 화면이다. 날짜를 맞추기 전에 옮긴다.
    const hiddenPageRedirect = await resolveHiddenMapPageRedirect(request.url);

    if (hiddenPageRedirect) {
      return redirect(hiddenPageRedirect);
    }

    const application = getApplicationTypeAndName(params.application!);

    let configuration: Configuration | undefined;
    try {
      configuration = await getConfiguration<Configuration>();
    } catch {
      // Configuration fetch may fail when the backend is down.
      // Continue with defaults so that date params are still redirected.
    }

    // application을 고르기 전에는 map을 그리지 않으므로 날짜를 채울 필요가 없다.
    if (!application?.applicationName || !application.serviceType) {
      return application;
    }

    const destination = resolveServerMapDateRedirect({
      basePath: `${pagePath}/${params.application}`,
      requestUrl: request.url,
      periodMax: configuration?.['periodMax.serverMap'],
    });

    return destination ? redirect(destination) : application;
  };

export const serverMapRouteLoader = createMapRouteLoader(APP_PATH.SERVER_MAP);
