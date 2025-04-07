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
} from '@pinpoint-fe/ui/src/utils';
import { format, parse } from 'date-fns';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const scatterOrHeatmapFullScreenLoader = async ({ params, request }: LoaderFunctionArgs) => {
  const application = getApplicationTypeAndName(params.application);
  const configuration = await getConfiguration<Configuration>();

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
      from: format(defaultParsedDateRange.from, SEARCH_PARAMETER_DATE_FORMAT),
      to: format(defaultParsedDateRange.to, SEARCH_PARAMETER_DATE_FORMAT),
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
};

export const scatterOrHeatmapFullScreenRealtimeLoader = ({
  params,
  request,
}: LoaderFunctionArgs) => {
  const application = getApplicationTypeAndName(params.application!);
  const url = new URL(request.url);
  const pathname = url?.pathname?.split('/')?.[1] || '';
  const queryParam = Object.fromEntries(url?.searchParams);
  const queryParamKeys = Object.keys(queryParam);

  if (queryParamKeys.filter((key) => key !== 'agentId').length > 0) {
    return redirect(`/${pathname}/${params.application}?agentId=${queryParam.agentId}`);
  }

  return application;
};
