import {
  APP_PATH,
  Configuration,
  SEARCH_PARAMETER_DATE_FORMAT,
} from '@pinpoint-fe/ui/src/constants';
import { getConfiguration, getRequestService, getServices } from '@pinpoint-fe/ui/src/hooks';
import { isValidDateRange, parseSystemMetricPath, toRouterPath } from '@pinpoint-fe/ui/src/utils';
import { getParsedDateRange, getTimezone } from '@pinpoint-fe/ui/src/utils';
import { parse } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { LoaderFunctionArgs, redirect } from 'react-router';

/**
 * 세그먼트 하나뿐인 `/systemMetric/X`의 X가 service인지. 모양으로는 hostGroup과 구별할 수 없어
 * service 목록과 대조한다. 목록을 못 읽으면 service로 본다 — 표준 형태 그대로이므로 경로를 바꾸지
 * 않는다(모르는 채로 hostGroup으로 단정해 옮기면 되돌릴 수 없다).
 */
const isServiceSegment = async (encodedSegment: string) => {
  let segment = encodedSegment;
  try {
    segment = decodeURIComponent(encodedSegment);
  } catch {
    // 잘못된 인코딩이면 원본으로 대조한다.
  }

  try {
    return (await getServices()).includes(segment);
  } catch {
    return true;
  }
};

export const systemMetricRouteLoader = async ({ request }: LoaderFunctionArgs) => {
  const requestUrl = new URL(request.url);

  let configuration: Configuration | undefined;
  try {
    configuration = await getConfiguration<Configuration>();
  } catch {
    // Continue with defaults so that date params are still redirected.
  }

  const timezone = getTimezone();
  // 설정을 읽은 뒤에 불러야 한다. undefined면 enableServiceMap이 꺼져 있다 — service 개념이 없다.
  // 지금 경로로 판정하지 않는다: 첫 로드에서는 `getRequestService`가 바로 이 경로를 읽는데, 세그먼트
  // 하나뿐인 `/systemMetric/{hostGroup}`의 hostGroup을 service로 읽어 `/systemMetric/{hostGroup}/
  // {hostGroup}`으로 옮기게 된다. 경로에 실린 service는 아래에서 service 목록과 대조해 따로 가리고,
  // 여기서는 serviceName을 싣지 않은 경로로 판정해 지금 보고 있는 service(전역 선택값)를 받는다.
  const requestService = getRequestService(APP_PATH.SYSTEM_METRIC);
  const enableServiceMap = !!requestService;
  // `request.url`에는 라우터 basename(`BASE_PATH`)이 붙어 있다. 떼지 않으면 세그먼트가 어긋난다.
  const { encodedServiceName, hostGroupName } = parseSystemMetricPath(
    toRouterPath(requestUrl.pathname),
    enableServiceMap,
  );

  // 경로에 serviceName이 없으면 지금 보고 있는 service를 붙여 표준 형태로 옮긴다(servicemap 로더와
  // 같은 규칙). 세그먼트가 하나뿐이면 세그먼트가 생기기 전 형태(`/systemMetric/{hostGroup}`)일 수 있다.
  if (requestService) {
    const currentServiceSegment = encodeURIComponent(requestService);

    if (!encodedServiceName) {
      return redirect(`${APP_PATH.SYSTEM_METRIC}/${currentServiceSegment}${requestUrl.search}`);
    }

    if (!hostGroupName && !(await isServiceSegment(encodedServiceName))) {
      return redirect(
        `${APP_PATH.SYSTEM_METRIC}/${currentServiceSegment}/${encodedServiceName}${requestUrl.search}`,
      );
    }
  }

  const hostGroup = hostGroupName || null;

  if (hostGroup) {
    // 들어온 경로의 형태(serviceName 세그먼트 유무)를 그대로 유지한다.
    const basePath = `${APP_PATH.SYSTEM_METRIC}${
      encodedServiceName ? `/${encodedServiceName}` : ''
    }/${hostGroup}`;
    const queryParam = Object.fromEntries(requestUrl.searchParams);
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
    const validateDateRange = isValidDateRange(configuration?.['periodMax.systemMetric'] || 28);
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
        return hostGroup;
      } else {
        return redirect(defaultDestination);
      }
    }
  }

  return hostGroup;
};
