import {
  APP_PATH,
  Configuration,
  SEARCH_PARAMETER_DATE_FORMAT,
} from '@pinpoint-fe/ui/src/constants';
import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import {
  getApplicationTypeAndName,
  getParsedDateRange,
  isValidDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { parse, format } from 'date-fns';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const transactionRouteLoader = async ({ params, request }: LoaderFunctionArgs) => {
  try {
    const application = getApplicationTypeAndName(params.application!);
    const configuration = await getConfiguration<Configuration>();

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
        from: format(defaultParsedDateRange.from, SEARCH_PARAMETER_DATE_FORMAT),
        to: format(defaultParsedDateRange.to, SEARCH_PARAMETER_DATE_FORMAT),
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
  } catch (err) {
    console.error('Error in transactionRouteLoader:', err);
    return null;
  }
};
