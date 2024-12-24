import { APP_PATH, SEARCH_PARAMETER_DATE_FORMAT } from '@pinpoint-fe/ui/constants';
import { isValidDateRange } from '@pinpoint-fe/ui/utils';
import { getParsedDateRange } from '@pinpoint-fe/ui/utils';
import { parse, format } from 'date-fns';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const systemMetricRouteLoader = ({ params, request }: LoaderFunctionArgs) => {
  const hostGroup = params.hostGroup || null;

  if (hostGroup) {
    const basePath = `${APP_PATH.SYSTEM_METRIC}/${hostGroup}`;
    const queryParam = Object.fromEntries(new URL(request.url).searchParams);
    const conditions = Object.keys(queryParam);

    const from = queryParam?.from as string;
    const to = queryParam?.to as string;

    const currentDate = new Date();
    const parsedDateRange = {
      from: parse(from, SEARCH_PARAMETER_DATE_FORMAT, currentDate),
      to: parse(to, SEARCH_PARAMETER_DATE_FORMAT, currentDate),
    };
    const defaultParsedDateRange = getParsedDateRange({ from, to });
    const defaultFormattedDateRange = {
      from: format(defaultParsedDateRange.from, SEARCH_PARAMETER_DATE_FORMAT),
      to: format(defaultParsedDateRange.to, SEARCH_PARAMETER_DATE_FORMAT),
    };
    const validateDateRange = isValidDateRange(28);
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
