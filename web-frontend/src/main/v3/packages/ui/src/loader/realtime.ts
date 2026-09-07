import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';
import { LoaderFunctionArgs, redirect } from 'react-router';
import { resolveHiddenMapPageRedirect } from './hiddenMapPage';

export const realtimeLoader = async ({ params, request }: LoaderFunctionArgs) => {
  try {
    // servicemap이 켜져 있으면 servermap 실시간 보기도 메뉴에서 감춘 화면이다.
    const hiddenPageRedirect = await resolveHiddenMapPageRedirect(request.url);

    if (hiddenPageRedirect) {
      return redirect(hiddenPageRedirect);
    }

    const application = getApplicationTypeAndName(params.application!);
    const searchParams = new URL(request.url).searchParams;
    const paramCount = [...new Set(searchParams.keys())].length;

    if (paramCount > 0) {
      return redirect(`/serverMap/realtime/${params.application}`);
    }

    return application;
  } catch (err) {
    console.error('Error in realtimeLoader:', err);
    return null;
  }
};
