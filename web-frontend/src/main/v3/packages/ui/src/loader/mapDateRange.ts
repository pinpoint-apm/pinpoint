import {
  Configuration,
  SEARCH_PARAMETER_DATE_FORMAT,
  SEARCH_PARAMETER_DATE_FORMAT_WHITE_LIST,
} from '@pinpoint-fe/ui/src/constants';
import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import {
  convertParamsToQueryString,
  getFormattedDateRange,
  getParsedDateRange,
  getTimezone,
  isValidDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { parse } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';

/**
 * map 계열 화면의 from/to를 표준 형식(`SEARCH_PARAMETER_DATE_FORMAT`)으로 맞춰야 하는지 판단해,
 * 맞춰야 하면 리다이렉트 목적지를, 이미 맞으면 undefined를 반환한다.
 *
 * servicemap 로더와 filteredMap 로더가 공유한다. 둘 다 servermap이 빠진 뒤에도 남는 화면이므로,
 * 날짜 규칙이 바뀔 때 고칠 곳이 하나여야 한다. (servermap 로더의 `createMapRouteLoader`와 내용이
 * 같지만 그쪽은 파일째로 사라지므로 합치지 않는다 — 그때 "호출자가 하나뿐인 모듈"을 다시
 * 정리해야 한다.)
 *
 * from/to 외의 query string은 목적지에도 그대로 싣는다. filteredMap의 filter/hint처럼 그 화면을
 * 성립시키는 조건이 함께 실려 오기 때문에, 날짜를 고치면서 떨어뜨리면 필터가 사라진 map을 그린다.
 *
 * @param requestUrl 로더가 받은 요청 URL 전체.
 * @param basePath   목적지의 경로 부분. 호출자가 자기 화면의 세그먼트 규칙대로 만들어 넘긴다.
 */
export const resolveMapDateRangeRedirect = async ({
  requestUrl,
  basePath,
}: {
  requestUrl: string;
  basePath: string;
}): Promise<string | undefined> => {
  let configuration: Configuration | undefined;
  try {
    configuration = await getConfiguration<Configuration>();
  } catch {
    // Configuration fetch may fail when the backend is down.
    // Continue with defaults so that date params are still redirected.
  }

  const timezone = getTimezone();
  const queryParam = Object.fromEntries(new URL(requestUrl).searchParams);
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
  const defaultDestination = `${basePath}?${convertParamsToQueryString({
    ...queryParam,
    ...defaultFormattedDateRange,
  })}`;

  if (conditions.length === 0) {
    return defaultDestination;
  }

  if (!conditions.includes('from')) {
    return undefined;
  }

  if (!conditions.includes('to')) {
    return defaultDestination;
  }

  const matchedFormat = SEARCH_PARAMETER_DATE_FORMAT_WHITE_LIST.find((dateFormat) => {
    const parsedDateRange = {
      from: parse(from, dateFormat, currentDate),
      to: parse(to, dateFormat, currentDate),
    };

    return validationRange(parsedDateRange);
  });

  if (!matchedFormat) {
    return defaultDestination;
  }

  if (matchedFormat !== SEARCH_PARAMETER_DATE_FORMAT) {
    const formattedDataRange = getFormattedDateRange({
      from: parse(from, matchedFormat, currentDate),
      to: parse(to, matchedFormat, currentDate),
    });

    return `${basePath}?${convertParamsToQueryString({
      ...queryParam,
      ...formattedDataRange,
    })}`;
  }

  return undefined;
};
