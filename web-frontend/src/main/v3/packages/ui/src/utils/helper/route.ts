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
 * serviceName이 주어지면 application 세그먼트를 `{serviceName}@{applicationName}@{serviceType}`로
 * 만드는 경로 빌더. transaction 화면들은 새 탭으로 열리므로(map의 drag&drop → transactionList,
 * transactionList의 외부 링크 → transactionDetail), 어떤 service를 보던 중이었는지 URL에 남겨야
 * 그 화면의 모든 API에 pServiceName 헤더를 실을 수 있다.
 * 파싱은 `getServiceAndApplicationTypeAndName`이 담당한다.
 *
 * serviceName은 백엔드에서 형식이 검증되지 않으므로(`ServiceNameRequest`에 제약이 없다) '/'나
 * '@'가 들어올 수 있어 인코딩한다. 인코딩하지 않으면 '/'가 세그먼트를 쪼개 `:application?`
 * 라우트 매칭이 실패하고, '@'는 구분자와 구별되지 않는다. applicationName/serviceType은
 * 백엔드가 `[a-zA-Z0-9._\-]+`로 검증하므로(IdValidateUtils) 기존처럼 그대로 둔다.
 */
const getServiceScopedApplicationPath =
  (pagePath: string) =>
  (
    application?: ApplicationType | null,
    queryParams?: {
      [k: string]: string;
    },
    serviceName?: string,
  ) => {
    if (!application?.applicationName || !application?.serviceType) {
      return pagePath;
    }

    const servicePrefix = serviceName ? `${encodeURIComponent(serviceName)}@` : '';
    const queryString =
      queryParams?.from && queryParams?.to
        ? `?${convertParamsToQueryString({ from: queryParams.from, to: queryParams.to })}`
        : '';

    return `${pagePath}/${servicePrefix}${application.applicationName}@${application.serviceType}${queryString}`;
  };

/** /transactionList */
export const getTransactionListPath = getServiceScopedApplicationPath(APP_PATH.TRANSACTION_LIST);
/** /transactionDetail */
export const getTransactionDetailPath = getServiceScopedApplicationPath(
  APP_PATH.TRANSACTION_DETAIL,
);
export const getThreadDumpPath = getApplicationPath(APP_PATH.THREAD_DUMP);
