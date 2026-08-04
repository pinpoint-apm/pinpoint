import {
  ApplicationType,
  APP_PATH,
  GetServerMap,
  IMAGE_PATH,
  FilteredMapType as FilteredMap,
} from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '../string';

export const getServerImagePath = (application?: ApplicationType | GetServerMap.NodeData) => {
  return `${IMAGE_PATH}/servers/${application?.serviceType || 'UNKNOWN'}.png`;
};

export const getServerIconPath = (application?: ApplicationType | GetServerMap.NodeData) => {
  return `${IMAGE_PATH}/icons/${application?.serviceType || 'UNKNOWN'}.png`;
};

export const getApplicationPath =
  (pagePath: string) =>
  (
    application?: ApplicationType | null,
    queryParams?: {
      [k: string]: string;
    },
  ) => {
    let subPath = '';
    let queryString = '';
    if (application?.applicationName && application?.serviceType) {
      subPath = `/${application?.applicationName}@${application?.serviceType}`;
      if (queryParams?.from && queryParams?.to) {
        queryString = `${convertParamsToQueryString({
          from: queryParams?.from,
          to: queryParams?.to,
        })}`;
      }
      return `${pagePath}${subPath}${queryString ? `?${queryString}` : queryString}`;
    }

    return `${pagePath}`;
  };

export const getHostGroupPath =
  (pagePath: string) =>
  (
    hostGroup?: string | null,
    queryParams?: {
      [k: string]: string;
    },
  ) => {
    let subPath = '';
    let queryString = '';
    if (hostGroup) {
      subPath = `/${hostGroup}`;
      if (queryParams?.from && queryParams?.to) {
        queryString = `${convertParamsToQueryString({
          from: queryParams.from,
          to: queryParams.to,
        })}`;
      }
      return `${pagePath}${subPath}${queryString ? `?${queryString}` : queryString}`;
    }

    return `${pagePath}`;
  };

/** /serverMap */
export const getServerMapPath = getApplicationPath(APP_PATH.SERVER_MAP);
/** /serviceMap */
export const getServiceMapPath = getApplicationPath(APP_PATH.SERVICE_MAP);
/** /realtime */
export const getRealtimePath = getApplicationPath(APP_PATH.SERVER_MAP_REALTIME);
/** /scatterFullScreenMode */
export const getScatterFullScreenPath = getApplicationPath(APP_PATH.SCATTER_FULL_SCREEN);
/** /scatterFullScreenMode/realtime */
export const getScatterFullScreenRealtimePath = getApplicationPath(
  APP_PATH.SCATTER_FULL_SCREEN_REALTIME,
);
/** /heatmapFullScreenMode */
export const getHeatmapFullScreenPath = getApplicationPath(APP_PATH.HEATMAP_FULL_SCREEN);
/** /heatmapFullScreenMode/realtime */
export const getHeatmapFullScreenRealtimePath = getApplicationPath(
  APP_PATH.HEATMAP_FULL_SCREEN_REALTIME,
);
/** /filtedMap */
export const getFilteredMapPath = (filterState: FilteredMap.FilterState, soureIsWas?: boolean) => {
  let applicationNameAndType = '';
  if (filterState?.applicationName) {
    applicationNameAndType = `${filterState?.applicationName}@${filterState.serviceType}`;
  } else {
    if (soureIsWas) {
      applicationNameAndType = `${filterState?.fromApplication}@${filterState.fromServiceType}`;
    } else {
      applicationNameAndType = `${filterState?.toApplication}@${filterState.toServiceType}`;
    }
  }
  return `${APP_PATH.FILTERED_MAP}/${applicationNameAndType}`;
};

export const getErrorAnalysisPath = getApplicationPath(APP_PATH.ERROR_ANALYSIS);
export const getUrlStatPath = getApplicationPath(APP_PATH.URL_STATISTIC);
export const getInspectorPath = getApplicationPath(APP_PATH.INSPECTOR);
export const getOpenTelemetryPath = getApplicationPath(APP_PATH.OPEN_TELEMETRY_METRIC);
export const getSystemMetricPath = getHostGroupPath(APP_PATH.SYSTEM_METRIC);
/**
 * /transactionList
 *
 * serviceName이 주어지면 application 세그먼트를 `{serviceName}@{applicationName}@{serviceType}`로
 * 만든다. transactionList는 scatter/heatmap의 drag&drop으로 새 탭에서 열리므로, 어떤 service를
 * 보던 중이었는지 URL에 남겨야 그 화면의 모든 API에 pServiceName 헤더를 실을 수 있다.
 * 파싱은 `getServiceAndApplicationTypeAndName`이 담당한다.
 */
export const getTransactionListPath = (
  application?: ApplicationType | null,
  queryParams?: {
    [k: string]: string;
  },
  serviceName?: string,
) => {
  if (!application?.applicationName || !application?.serviceType) {
    return APP_PATH.TRANSACTION_LIST;
  }

  const servicePrefix = serviceName ? `${serviceName}@` : '';
  const queryString =
    queryParams?.from && queryParams?.to
      ? `?${convertParamsToQueryString({ from: queryParams.from, to: queryParams.to })}`
      : '';

  return `${APP_PATH.TRANSACTION_LIST}/${servicePrefix}${application.applicationName}@${application.serviceType}${queryString}`;
};
export const getTransactionDetailPath = getApplicationPath(APP_PATH.TRANSACTION_DETAIL);
export const getThreadDumpPath = getApplicationPath(APP_PATH.THREAD_DUMP);
