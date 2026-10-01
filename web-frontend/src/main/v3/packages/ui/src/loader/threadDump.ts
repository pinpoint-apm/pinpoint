import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { getConfiguration } from '@pinpoint-fe/ui/src/hooks';
import { LoaderFunctionArgs, redirect } from 'react-router';
import { resolveServiceScopedPage } from './serviceScopedPage';

export const threadDumpRouteLoader = async ({ request }: LoaderFunctionArgs) => {
  try {
    const requestUrl = new URL(request.url);

    try {
      await getConfiguration();
    } catch {
      // 설정을 못 읽으면 serviceName 세그먼트를 붙일지 알 수 없다. 경로를 그대로 둔다.
    }

    const { application, basePath, isServiceNameMissing } = resolveServiceScopedPage(
      APP_PATH.THREAD_DUMP,
      requestUrl.pathname,
    );

    if (application?.applicationName && application.serviceType) {
      const redirectPath = `${APP_PATH.SERVER_MAP}/${application.applicationName}@${application.serviceType}`;
      const agentId = requestUrl.searchParams.get('agentId');

      if (!agentId) {
        return redirect(redirectPath);
      }

      return isServiceNameMissing ? redirect(`${basePath}${requestUrl.search}`) : application;
    } else {
      return redirect(APP_PATH.SERVER_MAP);
    }
  } catch (err) {
    console.error('Error in threadDumpRouteLoader:', err);
    return null;
  }
};
