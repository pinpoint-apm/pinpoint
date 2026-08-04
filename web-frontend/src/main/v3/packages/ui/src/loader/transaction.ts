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
  // 세그먼트가 `{serviceName}@{applicationName}@{serviceType}`일 수 있으므로 serviceName을 분리한다.
  // 리다이렉트 경로는 원본 세그먼트(params.application)를 그대로 쓰므로 serviceName이 유지된다.
  const application = getServiceAndApplicationTypeAndName(params.application!);

  let configuration: Configuration | undefined;
  try {
    configuration = await getConfiguration<Configuration>();
  } catch {
    // Continue with defaults so that date params are still redirected.
  }

  const timezone = getTimezone();

  if (application?.applicationName && application.serviceType) {
    const basePath = `${APP_PATH.TRANSACTION_LIST}/${params.application}`;
    const queryParam = Object.fromEntries(new URL(request.url).searchParams);
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
