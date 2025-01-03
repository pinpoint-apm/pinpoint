import { APP_PATH, SEARCH_PARAMETER_DATE_FORMAT } from '@pinpoint-fe/ui/constants';
import { convertTimeStringToTime, convertParamsToQueryString } from '@pinpoint-fe/ui/utils';
import { parse, format, subMilliseconds } from 'date-fns';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const handleV2RouteLoader = ({ params, request }: LoaderFunctionArgs) => {
  const basePath = `${APP_PATH.SERVER_MAP}/${params.application}`;
  const v2QueryParams = Object.fromEntries(new URL(request.url).searchParams);
  const currentDate = new Date();

  const to = parse(params.endTime!, SEARCH_PARAMETER_DATE_FORMAT, currentDate);
  try {
    const timeDiff = convertTimeStringToTime(params.period!);
    const from = subMilliseconds(to, timeDiff);
    const formattedDateRange = {
      from: format(from, SEARCH_PARAMETER_DATE_FORMAT),
      to: format(to, SEARCH_PARAMETER_DATE_FORMAT),
    };
    const v3DateQueryString = convertParamsToQueryString({
      ...formattedDateRange,
      ...v2QueryParams,
    });

    return redirect(`${basePath}?${v3DateQueryString}`);
  } catch (e) {
    return redirect(`${basePath}`);
  }
};
