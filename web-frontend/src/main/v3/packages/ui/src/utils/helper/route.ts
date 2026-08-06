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
/**
 * /serviceMap/{serviceName}/{applicationName}@{serviceType}
 *
 * servicemap은 "어떤 service를 보는 중인지"가 URL의 진실의 원천이다. 그래서 DEFAULT도 예외 없이
 * 항상 싣는다. 전역 선택값(`selectedServiceAtom`)은 탭 간 공유 저장소라, 그것만 믿으면 링크를
 * 새 탭에서 열어 둔 뒤 원래 탭에서 service를 바꿨을 때 화면과 어긋난다.
 *
 * application 세그먼트는 DEFAULT service에서만 붙는다. 그 외 service는 소속 application을 모두
 * 모아 그리므로 기준 application이 없다.
 *
 * serviceName은 백엔드에서 형식이 검증되지 않으므로('ServiceNameRequest'에 제약이 없다) '/'나
 * '@'가 들어올 수 있어 인코딩한다. 인코딩하지 않으면 '/'가 세그먼트를 쪼개 라우트 매칭이 깨지고,
 * '@'는 application 세그먼트의 구분자와 구별되지 않는다.
 * (applicationName/serviceType은 백엔드가 `[a-zA-Z0-9._\-]+`로 검증하므로 그대로 둔다.)
 *
 * application이 없어도 기간(from/to)은 유지한다. 그 상태로도 map을 그리기 때문에,
 * 다른 화면에서 돌아올 때 보고 있던 기간이 기본값으로 초기화되면 안 된다.
 */
export const getServiceMapPath = (
  serviceName: string,
  application?: ApplicationType | null,
  queryParams?: {
    [k: string]: string;
  },
) => {
  const applicationSegment =
    application?.applicationName && application?.serviceType
      ? `/${application.applicationName}@${application.serviceType}`
      : '';
  const queryString =
    queryParams?.from && queryParams?.to
      ? `?${convertParamsToQueryString({ from: queryParams.from, to: queryParams.to })}`
      : '';

  return `${APP_PATH.SERVICE_MAP}/${encodeURIComponent(serviceName)}${applicationSegment}${queryString}`;
};
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
 * serviceName이 주어지면 `/{page}/{serviceName}/{applicationName}@{serviceType}`를 만드는 경로
 * 빌더. servicemap과 같은 세그먼트 표기다.
 *
 * transaction 화면들은 새 탭으로 열리므로(map의 drag&drop → transactionList, transactionList의
 * 외부 링크 → transactionDetail), 어떤 service를 보던 중이었는지 URL에 남겨야 그 화면의 모든
 * API에 pServiceName 헤더를 실을 수 있다. 전역 선택값은 탭 간 공유 저장소라 믿을 수 없다.
 *
 * enableServiceMap이 꺼져 있으면 service 개념이 없어 serviceName이 undefined로 들어온다.
 * 그때는 세그먼트를 붙이지 않아 예전과 같은 경로가 된다.
 *
 * serviceName은 백엔드에서 형식이 검증되지 않으므로(`ServiceNameRequest`에 제약이 없다) '/'나
 * '@'가 들어올 수 있어 인코딩한다. 인코딩하지 않으면 '/'가 세그먼트를 쪼개 라우트 매칭이 깨지고,
 * '@'는 application 세그먼트의 구분자와 구별되지 않는다. applicationName/serviceType은
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

    const serviceSegment = serviceName ? `/${encodeURIComponent(serviceName)}` : '';
    const queryString =
      queryParams?.from && queryParams?.to
        ? `?${convertParamsToQueryString({ from: queryParams.from, to: queryParams.to })}`
        : '';

    return `${pagePath}${serviceSegment}/${application.applicationName}@${application.serviceType}${queryString}`;
  };

/** /transactionList */
export const getTransactionListPath = getServiceScopedApplicationPath(APP_PATH.TRANSACTION_LIST);
/** /transactionDetail */
export const getTransactionDetailPath = getServiceScopedApplicationPath(
  APP_PATH.TRANSACTION_DETAIL,
);
export const getThreadDumpPath = getApplicationPath(APP_PATH.THREAD_DUMP);
