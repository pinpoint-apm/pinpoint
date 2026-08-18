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
 * `/{page}/{serviceName}/{applicationName}@{serviceType}?` 형태의 servicemap 계열 경로 빌더.
 *
 * servicemap은 "어떤 service를 보는 중인지"가 URL의 진실의 원천이다. 그래서 DEFAULT도 예외 없이
 * 항상 싣는다. 전역 선택값(`selectedServiceAtom`)은 탭 간 공유 저장소라, 그것만 믿으면 링크를
 * 새 탭에서 열어 둔 뒤 원래 탭에서 service를 바꿨을 때 화면과 어긋난다.
 *
 * serviceName은 백엔드에서 형식이 검증되지 않으므로('ServiceNameRequest'에 제약이 없다) '/'나
 * '@'가 들어올 수 있어 인코딩한다. 인코딩하지 않으면 '/'가 세그먼트를 쪼개 라우트 매칭이 깨지고,
 * '@'는 application 세그먼트의 구분자와 구별되지 않는다.
 * (applicationName/serviceType은 백엔드가 `[a-zA-Z0-9._\-]+`로 검증하므로 그대로 둔다.)
 */
const getServiceScopedMapPath =
  (pagePath: string) =>
  (
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

    return `${pagePath}/${encodeURIComponent(serviceName)}${applicationSegment}${queryString}`;
  };

/**
 * /serviceMap/{serviceName}/{applicationName}@{serviceType}
 *
 * application 세그먼트는 DEFAULT service에서만 붙는다. 그 외 service는 소속 application을 모두
 * 모아 그리므로 기준 application이 없다.
 *
 * application이 없어도 기간(from/to)은 유지한다. 그 상태로도 map을 그리기 때문에,
 * 다른 화면에서 돌아올 때 보고 있던 기간이 기본값으로 초기화되면 안 된다.
 */
export const getServiceMapPath = getServiceScopedMapPath(APP_PATH.SERVICE_MAP);
/** /realtime */
export const getRealtimePath = getApplicationPath(APP_PATH.SERVER_MAP_REALTIME);
/**
 * /serviceMap/realtime/{serviceName}/{applicationName}@{serviceType}
 *
 * servicemap의 실시간 보기. servermap의 실시간 보기와 화면이 같고 map을 그리는 API만 다르다
 * (`/api/servermap/serverMap` → `/api/servermap/serviceMap`).
 *
 * application 세그먼트는 map을 그리는 규칙을 그대로 따른다. DEFAULT service는 고른 application
 * 하나를 기준으로 그리므로 세그먼트가 붙고, 그 외 service는 소속 application을 모두 모아 그려
 * 기준 application이 없으므로 붙지 않는다(라우트 로더가 실려 들어온 세그먼트를 지운다).
 * 비DEFAULT에서 우측 패널(스캐터/액티브 스레드)의 조회 대상은 map에서 클릭한 노드로 정해진다.
 *
 * 기간은 "지금부터 5분 전"으로 화면이 직접 만들기 때문에 from/to는 싣지 않는다
 * (라우트 로더가 실려 들어온 query string을 지운다).
 */
export const getServiceMapRealtimePath = getServiceScopedMapPath(APP_PATH.SERVICE_MAP_REALTIME);
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
/**
 * 필터 대상에서 filteredMap의 기준 application을 고른다. 고를 수 없으면 null이다.
 *
 * 노드에 걸린 필터는 그 노드가, 링크에 걸린 필터는 sourceIsWas에 따라 출발지/도착지가 기준이다.
 * filteredMap은 기준 application 없이는 조회가 성립하지 않으므로(`useGetFilteredServerMapData`가
 * applicationName을 필수로 요구한다) 호출자는 null이면 화면을 열지 않는다.
 *
 * servicemap의 service group(접힌 service) 노드·링크가 여기에 해당한다. 그 id는 serviceName
 * 하나뿐이어서 어떤 application을 기준으로 삼을지 정해지지 않는다.
 *
 * **링크는 양쪽 끝이 모두 application이어야 한다.** 기준으로 삼을 한쪽만 보고 통과시키면
 * Application→Service 링크가 새어나간다 — 출발지가 WAS라 출발지를 기준으로 잡고 열리지만,
 * 도착지가 service group이라 `toApplication`이 빈 채로 필터가 반쪽만 걸린다. 어느 쪽이 기준이
 * 되는지(sourceIsWas)는 map에서 링크를 찾아야 정해지므로, 그것과 무관하게 양쪽을 다 본다.
 */
export const getFilterTargetApplication = (
  filterState: FilteredMap.FilterState,
  sourceIsWas?: boolean,
): ApplicationType | null => {
  // 노드에 걸린 필터.
  if (filterState?.applicationName) {
    return filterState.serviceType
      ? { applicationName: filterState.applicationName, serviceType: filterState.serviceType }
      : null;
  }

  // 링크에 걸린 필터.
  const fromApplication = filterState?.fromApplication;
  const fromServiceType = filterState?.fromServiceType;
  const toApplication = filterState?.toApplication;
  const toServiceType = filterState?.toServiceType;

  if (!fromApplication || !fromServiceType || !toApplication || !toServiceType) {
    return null;
  }

  return sourceIsWas
    ? { applicationName: fromApplication, serviceType: fromServiceType }
    : { applicationName: toApplication, serviceType: toServiceType };
};

/**
 * /filteredMap/{serviceName}?/{applicationName}@{serviceType}
 *
 * 필터를 거는 화면(map, filteredMap)이 그 결과를 볼 화면으로 넘기는 경로. application은 필터
 * 대상에서 뽑는다(노드는 그 노드, 링크는 sourceIsWas에 따라 출발지/도착지).
 *
 * serviceName은 servicemap에서 왔을 때만 주어진다. 그래야 새 탭에서 열려도 어떤 service를 보던
 * 중이었는지 URL에 남아 모든 조회에 pServiceName 헤더가 실린다(전역 선택값은 탭 간 공유
 * 저장소라 믿을 수 없다). servermap에서 왔을 때는 주어지지 않아 경로 형태가 예전과 같다.
 *
 * serviceName은 백엔드에서 형식이 검증되지 않으므로 인코딩한다. 이유는 `getServiceScopedMapPath`와
 * 같다. (applicationName/serviceType은 백엔드가 검증하므로 그대로 둔다.)
 *
 * TODO: DEFAULT가 아닌 service에서 어떤 형태여야 하는지는 아직 논의 중이다. 지금은 DEFAULT와
 * 똑같이 필터 대상 application을 싣는다(map은 service 전체를 그리지만, filteredMap은 기준
 * application 없이는 조회할 수 없다 — `useGetFilteredServerMapData`가 applicationName을 필수로
 * 요구한다). service 전체를 대상으로 필터를 걸 수 있게 할지 정해지면 그때 형태를 맞춘다.
 */
export const getFilteredMapPath = (
  filterState: FilteredMap.FilterState,
  sourceIsWas?: boolean,
  serviceName?: string,
) => {
  const target = getFilterTargetApplication(filterState, sourceIsWas);
  const serviceSegment = serviceName ? `/${encodeURIComponent(serviceName)}` : '';

  // 기준 application이 없으면 세그먼트를 붙이지 않는다(다른 경로 빌더와 같은 처리).
  if (!target) {
    return `${APP_PATH.FILTERED_MAP}${serviceSegment}`;
  }

  return `${APP_PATH.FILTERED_MAP}${serviceSegment}/${target.applicationName}@${target.serviceType}`;
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
