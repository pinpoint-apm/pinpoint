import {
  SEARCH_PARAMETER_DATE_FORMAT,
  APP_PATH,
  Configuration,
} from '@pinpoint-fe/ui/src/constants';
import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import {
  getApplicationTypeAndName,
  getParsedDateRange,
  isValidDateRange,
  getTimezone,
} from '@pinpoint-fe/ui/src/utils';
import { parse } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const scatterOrHeatmapFullScreenLoader = async ({ params, request }: LoaderFunctionArgs) => {
  try {
    const application = getApplicationTypeAndName(params.application);
    const configuration = await getConfiguration<Configuration>();
    const timezone = getTimezone();
    const url = new URL(request.url);
    const pathname = url?.pathname?.split('/')?.[1] || '';

    if (pathname && application?.applicationName && application.serviceType) {
      const basePath = `/${pathname}/${params.application}`;
      const queryParam = Object.fromEntries(url?.searchParams);
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
        from: formatInTimeZone(defaultParsedDateRange.from, timezone, SEARCH_PARAMETER_DATE_FORMAT),
        to: formatInTimeZone(defaultParsedDateRange.to, timezone, SEARCH_PARAMETER_DATE_FORMAT),
      };
      const defaultDatesQueryString = new URLSearchParams(defaultFormattedDateRange).toString();
      const defaultDestination = `${basePath}?${defaultDatesQueryString}`;

      if (conditions.length === 0) {
        return redirect(defaultDestination);
      } else if (conditions.includes('from')) {
        if (
          conditions.includes('to') &&
          isValidDateRange(configuration?.['periodMax.serverMap'] || 2)(parsedDateRange)
        ) {
          return application;
        } else {
          return redirect(defaultDestination);
        }
      }
    } else {
      return redirect('/');
    }

    return application;
  } catch (err) {
    console.error('Error in scatterOrHeatmapFullScreenLoader:', err);
    return null;
  }
};

export const scatterOrHeatmapFullScreenRealtimeLoader = ({
  params,
  request,
}: LoaderFunctionArgs) => {
  try {
    const application = getApplicationTypeAndName(params.application!);
    const url = new URL(request.url);
    const pathname = url?.pathname?.split('/')?.[1] || '';
    const queryParam = Object.fromEntries(url?.searchParams);
    const queryParamKeys = Object.keys(queryParam);

    if (queryParamKeys.filter((key) => key !== 'agentId').length > 0) {
      return redirect(`/${pathname}/${params.application}?agentId=${queryParam.agentId}`);
    }

    return application;
  } catch (err) {
    console.error('Error in scatterOrHeatmapFullScreenRealtimeLoader:', err);
    return null;
  }
};
