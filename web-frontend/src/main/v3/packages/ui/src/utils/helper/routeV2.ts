import { ApplicationType, APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { convertToTimeUnit, getParsedDateRange } from '../date';
import { getApplicationPath } from './route';

export const getV2Url = (pagePath: string) => `${location.protocol}//${location.host}${pagePath}`;
export const getV2ApplicationUrl =
  (pagePath: string) =>
  (
    application?: ApplicationType | null,
    queryParams?: {
      [k: string]: string;
    },
  ) => {
    let subPath = '';
    if (queryParams?.from && queryParams.to) {
      const { from, to } = getParsedDateRange({ from: queryParams.from, to: queryParams.to });
      const timeUnit = convertToTimeUnit(to.getTime() - from.getTime());
      subPath = `/${timeUnit}/${queryParams.to}`;
    }

    return `${getV2Url(getApplicationPath(pagePath)(application))}${subPath}`;
  };

export const getV2RealtimeUrl = getV2ApplicationUrl('/main/realtime');
export const getV2ScatterRealtimeUrl = getV2ApplicationUrl(APP_PATH.SCATTER_FULL_SCREEN_REALTIME);
export const getV2InspectorUrl = getV2ApplicationUrl(APP_PATH.INSPECTOR);
