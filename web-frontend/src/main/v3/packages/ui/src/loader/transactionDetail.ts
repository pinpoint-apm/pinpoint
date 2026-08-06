import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { getApplicationTypeAndName, getServiceNameFromPath } from '@pinpoint-fe/ui/src/utils';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const transactionDetailRouteLoader = ({ request }: LoaderFunctionArgs) => {
  try {
    // 경로는 `/transactionDetail/{serviceName}/{applicationName}@{serviceType}` 형태다.
    //
    // react-router의 `params`는 디코딩된 값이라 serviceName의 '%2F'가 '/'로 풀려 세그먼트 경계가
    // 어긋나므로, 인코딩된 원본 경로에서 읽는다.
    const { pathname } = new URL(request.url);
    const parsedApplication = getApplicationTypeAndName(pathname);
    const application = parsedApplication && {
      serviceName: getServiceNameFromPath(pathname),
      ...parsedApplication,
    };

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
  } catch (err) {
    console.error('Error in transactionDetailRouteLoader:', err);
    return null;
  }
};
