import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const transactionDetailRouteLoader = ({ params, request }: LoaderFunctionArgs) => {
  const application = getApplicationTypeAndName(params.application!);

  if (application?.applicationName && application.serviceType) {
    const queryParam = Object.fromEntries(new URL(request.url).searchParams);
    const conditions = Object.keys(queryParam);

    if (conditions.length === 0) {
      return redirect(APP_PATH.SERVER_MAP);
    } else {
      if (conditions.includes('transactionInfo')) {
        return application;
      } else {
        return redirect(APP_PATH.SERVER_MAP);
      }
    }
  }

  return application;
};
