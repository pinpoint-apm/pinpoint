import {
  SEARCH_PARAMETER_DATE_FORMAT,
  SEARCH_PARAMETER_DATE_FORMAT_WHITE_LIST,
} from '@pinpoint-fe/ui/src/constants';
import {
  convertParamsToQueryString,
  getApplicationTypeAndName,
  getFormattedDateRange,
  getParsedDateRange,
  isValidDateRange,
} from '@pinpoint-fe/ui/src/utils';
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
    const validationRange = isValidDateRange(2);
    const defaultParsedDateRange = getParsedDateRange({ from, to });
    const defaultFormattedDateRange = {
      from: format(defaultParsedDateRange.from, SEARCH_PARAMETER_DATE_FORMAT),
      to: format(defaultParsedDateRange.to, SEARCH_PARAMETER_DATE_FORMAT),
    };
    const defaultDatesQueryString = new URLSearchParams(defaultFormattedDateRange).toString();
    const defaultDestination = `${basePath}?${defaultDatesQueryString}`;

    if (conditions.length === 0) {
      return redirect(defaultDestination);
    } else if (conditions.includes('from')) {
      if (!conditions.includes('to')) {
        return redirect(defaultDestination);
      }

      const matchedFormat = SEARCH_PARAMETER_DATE_FORMAT_WHITE_LIST.find((dateFormat) => {
        const parsedDateRange = {
          from: parse(from, dateFormat, currentDate),
          to: parse(to, dateFormat, currentDate),
        };

        return validationRange(parsedDateRange);
      });

      if (!matchedFormat) {
        return redirect(defaultDestination);
      }

      if (matchedFormat !== SEARCH_PARAMETER_DATE_FORMAT) {
        const parsedDateRange = {
          from: parse(from, matchedFormat, currentDate),
          to: parse(to, matchedFormat, currentDate),
        };
        const formattedDataRange = getFormattedDateRange(parsedDateRange);
        const destination = `${basePath}?${convertParamsToQueryString({
          ...queryParam,
          ...formattedDataRange,
        })}`;
        return redirect(destination);
      }

      return application;
    }
  }

  return application;
};
