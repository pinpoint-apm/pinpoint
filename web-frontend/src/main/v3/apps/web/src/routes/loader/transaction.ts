import { APP_PATH, SEARCH_PARAMETER_DATE_FORMAT } from '@pinpoint-fe/ui/src/constants';
import {
  getApplicationTypeAndName,
  getParsedDateRange,
  isValidDateRange,
} from '@pinpoint-fe/ui/src/utils';
import { parse, format } from 'date-fns';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const transactionRouteLoader = ({ params, request }: LoaderFunctionArgs) => {
  const application = getApplicationTypeAndName(params.application!);

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
    const validateDateRange = isValidDateRange(2);
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
};
