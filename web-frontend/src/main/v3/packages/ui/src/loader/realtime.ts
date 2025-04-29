import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';
import { LoaderFunctionArgs, redirect } from 'react-router-dom';

export const realtimeLoader = ({ params, request }: LoaderFunctionArgs) => {
  try {
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
