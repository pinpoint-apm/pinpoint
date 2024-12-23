import { SEARCH_PARAMETER_DATE_FORMAT, APP_PATH } from '@pinpoint-fe/constants';
import {
  getApplicationTypeAndName,
  getParsedDateRange,
  isValidDateRange,
} from '@pinpoint-fe/ui/utils';
import { format, parse } from 'date-fns';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const scatterFullScreenLoader = ({ params, request }: LoaderFunctionArgs) => {
  const application = getApplicationTypeAndName(params.application);
  if (application?.applicationName && application.serviceType) {
    const basePath = `${APP_PATH.SCATTER_FULL_SCREEN}/${params.application}`;
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
  } else {
    return redirect('/');
  }

  return application;
};

export const scatterFullScreenRealtimeLoader = ({ params, request }: LoaderFunctionArgs) => {
  const application = getApplicationTypeAndName(params.application!);
  const queryParam = Object.fromEntries(new URL(request.url).searchParams);
  const queryParamKeys = Object.keys(queryParam);

  if (queryParamKeys.filter((key) => key !== 'agentId').length > 0) {
    return redirect(
      `${APP_PATH.SCATTER_FULL_SCREEN_REALTIME}/${params.application}?agentId=${queryParam.agentId}`,
    );
  }

  return application;
};
