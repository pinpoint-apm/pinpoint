import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { getServiceAndApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const transactionDetailRouteLoader = ({ params, request }: LoaderFunctionArgs) => {
  try {
    // 세그먼트가 `{serviceName}@{applicationName}@{serviceType}`일 수 있으므로 serviceName을
    // 분리한다. 리다이렉트가 세그먼트를 재사용하지 않으므로(항상 serverMap으로 보낸다)
    // transactionRouteLoader처럼 raw 세그먼트를 다시 읽을 필요는 없다.
    const application = getServiceAndApplicationTypeAndName(params.application!);

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
