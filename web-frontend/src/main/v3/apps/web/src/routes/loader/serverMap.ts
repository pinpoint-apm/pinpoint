import { SERVER_MAP_DATE_FORMAT } from '@pinpoint-fe/constants';
import {
  getApplicationTypeAndName,
  getParsedDateRange,
  isValidDateRange,
} from '@pinpoint-fe/utils';
import { parse, format } from 'date-fns';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const serverMapRouteLoader = ({ params, request }: LoaderFunctionArgs) => {
  const application = getApplicationTypeAndName(params.application!);

  if (application?.applicationName && application.serviceType) {
    const basePath = `/serverMap/${params.application}`;
    const queryParam = Object.fromEntries(new URL(request.url).searchParams);
    const conditions = Object.keys(queryParam);

    const from = queryParam?.from as string;
    const to = queryParam?.to as string;

    const currentDate = new Date();
    const parsedDateRange = {
      from: parse(from, SERVER_MAP_DATE_FORMAT, currentDate),
      to: parse(to, SERVER_MAP_DATE_FORMAT, currentDate),
    };
    const defaultParsedDateRange = getParsedDateRange({ from, to });
    const defaultFormattedDateRange = {
      from: format(defaultParsedDateRange.from, SERVER_MAP_DATE_FORMAT),
      to: format(defaultParsedDateRange.to, SERVER_MAP_DATE_FORMAT),
    };
    const defaultDatesQueryString = new URLSearchParams(defaultFormattedDateRange).toString();
    const defaultDestination = `${basePath}?${defaultDatesQueryString}`;

    if (conditions.length === 0) {
      return redirect(defaultDestination);
    } else if (conditions.includes('from')) {
      if (conditions.includes('to') && isValidDateRange(2)(parsedDateRange)) {
        return application;
      } else {
        return redirect(defaultDestination);
      }
    }
  }

  return application;
};
