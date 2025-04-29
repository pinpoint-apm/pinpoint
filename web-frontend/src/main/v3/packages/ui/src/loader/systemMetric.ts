import {
  APP_PATH,
  Configuration,
  SEARCH_PARAMETER_DATE_FORMAT,
} from '@pinpoint-fe/ui/src/constants';
import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import { isValidDateRange } from '@pinpoint-fe/ui/src/utils';
import { getParsedDateRange } from '@pinpoint-fe/ui/src/utils';
import { parse, format } from 'date-fns';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const systemMetricRouteLoader = async ({ params, request }: LoaderFunctionArgs) => {
  try {
    const hostGroup = params.hostGroup || null;
    const configuration = await getConfiguration<Configuration>();

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
  } catch (err) {
    console.error('Error in systemMetricRouteLoader:', err);
    return null;
  }
};
