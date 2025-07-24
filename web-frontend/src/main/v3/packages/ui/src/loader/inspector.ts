import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import {
  APP_PATH,
  Configuration,
  SEARCH_PARAMETER_DATE_FORMAT,
} from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString, getTimezone } from '@pinpoint-fe/ui/src/utils';
import {
  getApplicationTypeAndName,
  getParsedDateRange,
  isValidDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { parse } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const inspectorRouteLoader = async ({ params, request }: LoaderFunctionArgs) => {
  try {
    const application = getApplicationTypeAndName(params.application!);
    const configuration = await getConfiguration<Configuration>();
    const timezone = getTimezone();

    if (application?.applicationName && application.serviceType) {
      const basePath = `${APP_PATH.INSPECTOR}/${params.application}`;
      const queryParam = Object.fromEntries(new URL(request.url).searchParams);
      const conditions = Object.keys(queryParam);

      const from = queryParam?.from as string;
      const to = queryParam?.to as string;

      const currentDate = new Date();
      const parsedDateRange = {
        from: parse(from, SEARCH_PARAMETER_DATE_FORMAT, currentDate),
        to: parse(to, SEARCH_PARAMETER_DATE_FORMAT, currentDate),
      };
      const validateDateRange = isValidDateRange(configuration?.['periodMax.inspector'] || 42);
      const defaultParsedDateRange = getParsedDateRange({ from, to }, validateDateRange);
      const defaultFormattedDateRange = {
        from: formatInTimeZone(defaultParsedDateRange.from, timezone, SEARCH_PARAMETER_DATE_FORMAT),
        to: formatInTimeZone(defaultParsedDateRange.to, timezone, SEARCH_PARAMETER_DATE_FORMAT),
      };
      const defaultDestination = `${basePath}?${convertParamsToQueryString({
        ...queryParam,
        ...defaultFormattedDateRange,
      })}`;

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
  } catch (err) {
    console.error('Error in inspectorRouteLoader:', err);
    return null;
  }
};
