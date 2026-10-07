import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';
import { toRouterPath, withoutTrailingSlash } from '@pinpoint-fe/ui/src/utils';
import { LoaderFunctionArgs, redirect } from 'react-router';

/**
 * Fills in the serviceName segment of an alarm page path, by the rule of the other loaders
 * (`resolveServiceScopedPage`): a path without the segment moves to the service the viewer is
 * looking at, so a copy opened in a new tab queries the same service.
 *
 * A history link sent before the segment named the service of the rule in `?serviceName=`. That
 * value comes first. The service the viewer is looking at can be another one, and the rule is not
 * there.
 *
 * With `enableServiceMap` off, `getRequestService` gives undefined and the path stays as it is.
 * The decision needs the configuration, so `getConfiguration` runs first.
 *
 * It takes the page path, so that an app can use it for an alarm page of its own.
 */
export const createAlarmRouteLoader =
  (pagePath: string) =>
  async ({ request }: LoaderFunctionArgs) => {
    try {
      const requestUrl = new URL(request.url);

      if (withoutTrailingSlash(toRouterPath(requestUrl.pathname)) !== pagePath) {
        return null;
      }

      try {
        await getConfiguration();
      } catch {
        // The stored value of enableServiceMap can still decide, as in the other loaders.
      }

      const requestService = getRequestService();
      if (!requestService) {
        return null;
      }

      const linkedService = requestUrl.searchParams.get('serviceName');
      if (linkedService) {
        requestUrl.searchParams.delete('serviceName');
      }
      const search = linkedService
        ? requestUrl.searchParams.toString().replace(/^(?=.)/, '?')
        : requestUrl.search;

      return redirect(
        `${pagePath}/${encodeURIComponent(linkedService || requestService)}${search}`,
      );
    } catch (err) {
      console.error('Error in alarmRouteLoader:', err);
      return null;
    }
  };

export const alarmRouteLoader = createAlarmRouteLoader(APP_PATH.CONFIG_ALARM);
