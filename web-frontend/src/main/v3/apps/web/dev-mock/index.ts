/**
 * [MOCK #10497] 로컬 개발 서버(vite dev)에서만 동작하는 임시 mock API.
 *
 * 이슈 #10497("servicemap에서 다른 service의 application을 고르면 그 service로 조회해야 한다")을
 * 로컬에서 확인하려면 service가 둘 이상이고 서로 호출하는 저장소가 필요하다. 그런 저장소를
 * 준비하는 대신 dev 서버가 그 응답을 대신 내려준다.
 *
 * 기본값은 꺼져 있고, `MOCK_SERVICE_MAP=1`일 때만 붙는다(= `yarn dev:mock`).
 * 가로채지 않는 `/api` 요청은 그대로 실제 백엔드로 프록시되므로, 켜 두어도 나머지 화면은 평소대로 쓸 수 있다.
 *
 * 이 디렉터리는 통째로 지울 수 있다. 지우는 방법은 README.md 참고.
 */

/* eslint-disable no-console */

import type { Plugin } from 'vite';
import {
  MOCK_APPLICATIONS,
  MOCK_SERVICES,
  OTHER_SERVICE,
  VIEWING_SERVICE,
  makeAgentOverviewResponse,
  makeApdexScoreResponse,
  makeHeatmapResponse,
  makeHistogramStatisticsResponse,
  makeScatterResponse,
  makeServiceMapResponse,
} from './mockData';

/** 가로채지 않고 실제 백엔드로 넘긴다는 표시. */
const PASS_THROUGH = Symbol('pass-through');

/** 실제 백엔드. vite proxy의 target과 같아야 한다. */
const UPSTREAM = process.env.MOCK_UPSTREAM || 'http://localhost:8080';
const UPSTREAM_TIMEOUT = 2000;

const LOG_PREFIX = '[mock #10497]';
const SERVICE_NAME_HEADER = 'pservicename';

interface MockContext {
  url: URL;
  /** 이 요청에 실려 온 pServiceName. 비어 있으면 헤더가 붙지 않은 것이다. */
  requestedServiceName: string;
}

type MockHandler = (context: MockContext) => Promise<unknown> | unknown;

const BASIC_ISO = /^(\d{4})(\d{2})(\d{2})T(\d{2})(\d{2})(\d{2})(\.\d+)?Z$/;

/** 프론트가 보내는 basic ISO(`20260821T031500Z`)와 epoch ms를 모두 받는다. */
const parseTime = (value: string | null, fallback: number) => {
  if (!value) {
    return fallback;
  }

  const matched = BASIC_ISO.exec(value);
  if (matched) {
    const [, year, month, day, hour, minute, second, ms = ''] = matched;
    return Date.parse(`${year}-${month}-${day}T${hour}:${minute}:${second}${ms}Z`);
  }

  const parsed = /^\d+$/.test(value) ? Number(value) : Date.parse(value);
  return Number.isNaN(parsed) ? fallback : parsed;
};

const getRange = (url: URL) => {
  const now = Date.now();
  const from = parseTime(url.searchParams.get('from'), now - 5 * 60 * 1000);
  const to = parseTime(url.searchParams.get('to'), now);
  return { from, to };
};

/** 조회 대상 application. 파라미터 이름이 API마다 다르다. */
const getApplicationName = (url: URL) =>
  url.searchParams.get('applicationName') || url.searchParams.get('application') || '';

const fetchUpstream = async <T>(path: string): Promise<T | undefined> => {
  try {
    const response = await fetch(`${UPSTREAM}${path}`, {
      signal: AbortSignal.timeout(UPSTREAM_TIMEOUT),
    });
    return response.ok ? ((await response.json()) as T) : undefined;
  } catch {
    // 백엔드가 안 떠 있어도 mock 화면은 볼 수 있어야 하므로 조용히 폴백한다.
    return undefined;
  }
};

/**
 * 백엔드가 안 떠 있을 때 쓰는 최소 configuration.
 * 실제 백엔드가 떠 있으면 그 응답에 `enableServiceMap`만 켜서 내려준다.
 */
const FALLBACK_CONFIGURATION = {
  webhookEnable: false,
  showActiveThread: true,
  showActiveThreadDump: false,
  sendUsage: false,
  editUserInfo: false,
  enableServerMapRealTime: true,
  showApplicationStat: true,
  showStackTraceOnError: true,
  showSystemMetric: false,
  showUrlStat: false,
  showExceptionTrace: false,
  showOtlpMetric: false,
  showInspector: true,
  showHeatmap: true,
  openSource: true,
  version: 'mock',
  'experimental.enableHeatmap.value': true,
  'experimental.enableHeatmap.description': 'mock',
  'experimental.enableServerMapRealTime.value': true,
  'experimental.enableServerMapRealTime.description': 'mock',
  'experimental.enableServerSideScanForScatter.value': false,
  'experimental.enableServerSideScanForScatter.description': 'mock',
  'experimental.useStatisticsAgentState.value': true,
  'experimental.useStatisticsAgentState.description': 'mock',
  'experimental.sampleScatter.value': false,
  'experimental.sampleScatter.description': 'mock',
  'experimental.enableServiceMap.value': true,
  'periodMax.exceptionTrace': 28,
  'periodInterval.exceptionTrace': ['5m', '20m', '1h', '3h', '6h', '12h', '1d', '2d'],
  'periodMax.inspector': 28,
  'periodInterval.inspector': ['5m', '20m', '1h', '3h', '6h', '12h', '1d', '2d'],
  'periodMax.otlpMetric': 28,
  'periodInterval.otlpMetric': ['5m', '20m', '1h', '3h', '6h', '12h', '1d', '2d'],
  'periodMax.serverMap': 2,
  'periodInterval.serverMap': ['5m', '20m', '1h', '3h', '6h', '12h', '1d', '2d'],
  'periodMax.systemMetric': 28,
  'periodInterval.systemMetric': ['5m', '20m', '1h', '3h', '6h', '12h', '1d', '2d'],
  'periodMax.uriStat': 28,
  'periodInterval.uriStat': ['5m', '20m', '1h', '3h', '6h', '12h', '1d', '2d'],
};

/**
 * mock이 응답할 조회 대상 이름.
 *
 * service group 노드(접힌 service)를 그대로 고르면 그 노드의 applicationName이 serviceName이라
 * `B`로 조회가 나간다. 그것까지 받아 주지 않으면 실제 백엔드가 400을 돌려주고 화면에 에러 토스트가
 * 떠서, 정작 확인하려는 흐름이 가려진다.
 */
const MOCK_TARGET_NAMES = [...MOCK_APPLICATIONS, ...MOCK_SERVICES];

/**
 * application 단위 조회는 mock 대상일 때만 가로챈다.
 * 그 외에는 실제 백엔드로 넘겨서, mock을 켜 둔 채로도 평소 화면을 그대로 볼 수 있게 한다.
 */
const forMockApplication =
  (build: (applicationName: string, context: MockContext) => unknown): MockHandler =>
  (context) => {
    const applicationName = getApplicationName(context.url);
    return MOCK_TARGET_NAMES.includes(applicationName)
      ? build(applicationName, context)
      : PASS_THROUGH;
  };

const handlers: Record<string, MockHandler> = {
  // 설정이 꺼져 있으면 화면에 service 개념 자체가 없으므로 여기서 강제로 켠다.
  '/api/configuration': async () => {
    const upstream = await fetchUpstream<Record<string, unknown>>('/api/configuration');
    return {
      ...(upstream ?? FALLBACK_CONFIGURATION),
      'experimental.enableServiceMap.value': true,
    };
  },

  // service 선택 박스에 mock service가 보이도록 실제 목록에 덧붙인다.
  '/api/v2/services': async () => {
    const upstream = (await fetchUpstream<string[]>('/api/v2/services')) ?? ['DEFAULT'];
    const merged = [...upstream];
    MOCK_SERVICES.forEach((service) => {
      if (!merged.includes(service)) {
        merged.push(service);
      }
    });
    return merged;
  },

  // mock service를 보고 있을 때만 map을 대신 내려준다.
  '/api/servermap/serviceMap': ({ url, requestedServiceName }) => {
    if (!MOCK_SERVICES.includes(requestedServiceName)) {
      return PASS_THROUGH;
    }
    const { from, to } = getRange(url);
    return makeServiceMapResponse(from, to);
  },

  '/api/histogram/statistics': forMockApplication(
    (applicationName, { url, requestedServiceName }) => {
      const { from, to } = getRange(url);
      return makeHistogramStatisticsResponse(applicationName, requestedServiceName, from, to);
    },
  ),

  '/api/histogram/statistics/links': forMockApplication(
    (applicationName, { url, requestedServiceName }) => {
      const { from, to } = getRange(url);
      return makeHistogramStatisticsResponse(applicationName, requestedServiceName, from, to);
    },
  ),

  '/api/getApdexScore': forMockApplication((applicationName, { requestedServiceName }) =>
    makeApdexScoreResponse(applicationName, requestedServiceName),
  ),

  '/api/agents/overview': forMockApplication((applicationName, { url, requestedServiceName }) => {
    const { to } = getRange(url);
    return makeAgentOverviewResponse(applicationName, requestedServiceName, to);
  }),

  '/api/getScatterData': forMockApplication((applicationName, { url, requestedServiceName }) => {
    const { from, to } = getRange(url);
    return makeScatterResponse(applicationName, requestedServiceName, from, to);
  }),

  '/api/heatmap/applicationData': forMockApplication(
    (applicationName, { url, requestedServiceName }) => {
      const { from, to } = getRange(url);
      return makeHeatmapResponse(applicationName, requestedServiceName, from, to);
    },
  ),
};

const isEnabled = () =>
  ['1', 'true', 'on'].includes(String(process.env.MOCK_SERVICE_MAP ?? '').toLowerCase());

export const serviceMapMockPlugin = (): Plugin => ({
  name: 'pinpoint-dev-mock-service-map',
  apply: 'serve',
  configureServer(server) {
    if (!isEnabled()) {
      return;
    }

    console.log(
      `${LOG_PREFIX} mock API 활성화. service "${VIEWING_SERVICE}"의 servicemap에 service ` +
        `"${OTHER_SERVICE}"가 group 노드로 그려집니다. → /serviceMap/${VIEWING_SERVICE}`,
    );

    server.middlewares.use(async (req, res, next) => {
      const path = req.url ?? '/';
      if (!path.startsWith('/api')) {
        next();
        return;
      }

      const url = new URL(path, 'http://localhost');
      const requestedServiceName = String(req.headers[SERVICE_NAME_HEADER] ?? '');
      const handler = handlers[url.pathname];

      // 가로채지 않는 요청도 어떤 service로 나갔는지는 남긴다. 헤더 확인이 이 이슈의 핵심이다.
      const logSuffix = `pServiceName=${requestedServiceName || '(none)'}`;

      if (!handler) {
        console.log(`${LOG_PREFIX} pass  ${url.pathname} ${logSuffix}`);
        next();
        return;
      }

      try {
        const body = await handler({ url, requestedServiceName });

        if (body === PASS_THROUGH) {
          console.log(`${LOG_PREFIX} pass  ${url.pathname} ${logSuffix}`);
          next();
          return;
        }

        console.log(`${LOG_PREFIX} MOCK  ${url.pathname} ${logSuffix}`);
        res.statusCode = 200;
        res.setHeader('Content-Type', 'application/json; charset=utf-8');
        res.end(JSON.stringify(body));
      } catch (error) {
        console.error(`${LOG_PREFIX} handler error on ${url.pathname}`, error);
        next();
      }
    });
  },
});
