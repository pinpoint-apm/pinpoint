import { lazy } from 'react';
import { createBrowserRouter, redirect, RouteObject } from 'react-router';
import {
  serverMapRouteLoader,
  serviceMapRouteLoader,
  filteredMapRouteLoader,
  serviceMapRealtimeLoader,
  realtimeLoader,
  errorAnalysisRouteLoader,
  urlStatisticRouteLoader,
  systemMetricRouteLoader,
  transactionRouteLoader,
  transactionDetailRouteLoader,
  inspectorRouteLoader,
  threadDumpRouteLoader,
  handleV2RouteLoader,
  openTelemetryRouteLoader,
  scatterOrHeatmapFullScreenLoader,
  scatterOrHeatmapFullScreenRealtimeLoader,
} from '@pinpoint-fe/ui/src/loader';
import { BASE_PATH, APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { registerAppRoutes } from '@pinpoint-fe/ui/src/utils';
import { SideNavigationOutlet } from '@pinpoint-fe/web/src/components/Layout/SideNavigationOutlet';
import { InitialFetchOutlet } from '@pinpoint-fe/web/src/components/Layout/InitialFetchOutlet';
import { ConfigurationOutlet } from '@pinpoint-fe/web/src/components/Layout/ConfigurationOutlet';
import { RouteErrorFallback } from '@pinpoint-fe/ui/src/components/Error';

import ServerMap from '@pinpoint-fe/web/src/pages/ServerMap';
const ServiceMap = lazy(() => import('@pinpoint-fe/web/src/pages/ServiceMap'));
const ServiceMapRealtime = lazy(() => import('@pinpoint-fe/web/src/pages/ServiceMap/Realtime'));
const Realtime = lazy(() => import('@pinpoint-fe/web/src/pages/ServerMap/Realtime'));
const ScatterOrHeatmapFullScreen = lazy(
  () => import('@pinpoint-fe/web/src/pages/ScatterOrHeatmapFullScreen'),
);
const FilteredMap = lazy(() => import('@pinpoint-fe/web/src/pages/FilteredMap'));
const NotFound = lazy(() => import('@pinpoint-fe/web/src/pages/NotFound'));
const ErrorAnalysis = lazy(() => import('@pinpoint-fe/web/src/pages/ErrorAnalysis'));
const ApiCheck = lazy(() => import('@pinpoint-fe/web/src/pages/ApiCheck'));
const UrlStatistic = lazy(() => import('@pinpoint-fe/web/src/pages/UrlStatistic'));
const SystemMetric = lazy(() => import('@pinpoint-fe/web/src/pages/SystemMetric'));
const Experimentals = lazy(() => import('@pinpoint-fe/web/src/pages/config/Experimentals'));
const TransactionList = lazy(() => import('@pinpoint-fe/web/src/pages/TransactionList'));
const TransactionDetail = lazy(() => import('@pinpoint-fe/web/src/pages/TransactionDetail'));
const Inspector = lazy(() => import('@pinpoint-fe/web/src/pages/Inspector'));
const ThreadDump = lazy(() => import('@pinpoint-fe/web/src/pages/ThreadDump'));
const OpenTelemetry = lazy(() => import('@pinpoint-fe/web/src/pages/OpenTelemetry'));
const General = lazy(() => import('@pinpoint-fe/web/src/pages/config/General'));
const Help = lazy(() => import('@pinpoint-fe/web/src/pages/config/Help'));
const Installation = lazy(() => import('@pinpoint-fe/web/src/pages/config/Installation'));
const UserGroup = lazy(() => import('@pinpoint-fe/web/src/pages/config/UserGroup'));
const Users = lazy(() => import('@pinpoint-fe/web/src/pages/config/Users'));
const Alarm = lazy(() => import('@pinpoint-fe/web/src/pages/config/Alarm'));
const Webhook = lazy(() => import('@pinpoint-fe/web/src/pages/config/Webhook'));
const AgentManagement = lazy(() => import('@pinpoint-fe/web/src/pages/config/AgentManagement'));
const AgentStatistic = lazy(() => import('@pinpoint-fe/web/src/pages/config/AgentStatistic'));
const ServiceSetting = lazy(() => import('@pinpoint-fe/web/src/pages/config/ServiceSetting'));

const defaultLoader = () => {
  return redirect('/serverMap');
};

// 라우트 경로에 `:serviceName`이 있는 화면이 경로에서 serviceName을 읽는다. 그 판단을 이 정의에서
// 하므로(`registerAppRoutes`) 화면에 serviceName 세그먼트를 추가할 때 고칠 곳은 여기 하나다.
const routes: RouteObject[] = [
  {
    path: '/',
    loader: defaultLoader,
  },
  {
    path: '/main',
    children: [
      {
        path: '',
        loader: defaultLoader,
      },
      {
        path: ':application/:period/:endTime',
        loader: handleV2RouteLoader,
      },
    ],
  },
  {
    element: <SideNavigationOutlet />,
    children: [
      {
        path: `${APP_PATH.API_CHECK}`,
        element: <ApiCheck />,
      },
      {
        element: <InitialFetchOutlet />,
        errorElement: <RouteErrorFallback />,
        children: [
          {
            errorElement: <RouteErrorFallback />,
            children: [
              {
                path: `${APP_PATH.SERVER_MAP}/:application?`,
                element: <ServerMap />,
                loader: serverMapRouteLoader,
              },
              {
                path: `${APP_PATH.SERVER_MAP_REALTIME}/:application?`,
                element: <Realtime />,
                loader: realtimeLoader,
              },
              {
                // `/serviceMap/:serviceName?`보다 세그먼트가 더 구체적이라 이 라우트가 먼저
                // 매칭된다(react-router의 경로 랭킹). 기준 application은 DEFAULT service에서만
                // 쓰고 그 외 service는 로더가 지우므로 두 세그먼트 모두 optional이다.
                path: `${APP_PATH.SERVICE_MAP_REALTIME}/:serviceName?/:application?`,
                element: <ServiceMapRealtime />,
                loader: serviceMapRealtimeLoader,
              },
              {
                // serviceName을 별도 세그먼트로 싣는다. DEFAULT가 아닌 service는 기준
                // application이 없으므로 두 세그먼트 모두 optional이다.
                path: `${APP_PATH.SERVICE_MAP}/:serviceName?/:application?`,
                element: <ServiceMap />,
                loader: serviceMapRouteLoader,
              },
              {
                // servicemap에서 넘어오면 serviceName이 함께 실린다. servermap에서 넘어온
                // 형태(`/filteredMap/{application}`)도 그대로 받으므로 두 세그먼트 모두
                // optional이다.
                path: `${APP_PATH.FILTERED_MAP}/:serviceName?/:application?`,
                element: <FilteredMap />,
                loader: filteredMapRouteLoader,
              },
              {
                // serviceName을 별도 세그먼트로 싣는다(servicemap·transaction과 같은 표기).
                // 세그먼트가 생기기 전 형태(`/scatterFullScreenMode/{application}`)의 링크도
                // 그대로 받으므로 두 세그먼트 모두 optional이다.
                path: `${APP_PATH.SCATTER_FULL_SCREEN}/:serviceName?/:application?`,
                element: <ScatterOrHeatmapFullScreen />,
                loader: scatterOrHeatmapFullScreenLoader,
              },
              {
                path: `${APP_PATH.SCATTER_FULL_SCREEN_REALTIME}/:serviceName?/:application?`,
                element: <ScatterOrHeatmapFullScreen />,
                loader: scatterOrHeatmapFullScreenRealtimeLoader,
              },
              {
                path: `${APP_PATH.HEATMAP_FULL_SCREEN}/:serviceName?/:application?`,
                element: <ScatterOrHeatmapFullScreen />,
                loader: scatterOrHeatmapFullScreenLoader,
              },
              {
                path: `${APP_PATH.HEATMAP_FULL_SCREEN_REALTIME}/:serviceName?/:application?`,
                element: <ScatterOrHeatmapFullScreen />,
                loader: scatterOrHeatmapFullScreenRealtimeLoader,
              },
              {
                // 아래 application 단위 화면들도 serviceName을 별도 세그먼트로 싣는다(servicemap과
                // 같은 표기). 세그먼트가 생기기 전 형태(`/inspector/{application}`)의 링크도 받으므로
                // 두 세그먼트 모두 optional이고, 로더가 serviceName을 채워 표준 형태로 옮긴다.
                path: `${APP_PATH.ERROR_ANALYSIS}/:serviceName?/:application?`,
                element: <ErrorAnalysis />,
                loader: errorAnalysisRouteLoader,
              },
              {
                path: `${APP_PATH.URL_STATISTIC}/:serviceName?/:application?`,
                element: <UrlStatistic />,
                loader: urlStatisticRouteLoader,
              },
              {
                // serviceName은 hostGroup과 모양으로 구별되지 않아 경로 형태를 enableServiceMap이
                // 정한다(`parseSystemMetricPath`). 로더가 세그먼트가 생기기 전 형태를 옮긴다.
                path: `${APP_PATH.SYSTEM_METRIC}/:serviceName?/:hostGroup?`,
                element: <SystemMetric />,
                loader: systemMetricRouteLoader,
              },
              {
                // serviceName을 별도 세그먼트로 싣는다(servicemap과 같은 표기).
                path: `${APP_PATH.TRANSACTION_LIST}/:serviceName?/:application?`,
                element: <TransactionList />,
                loader: transactionRouteLoader,
              },
              {
                path: `${APP_PATH.TRANSACTION_DETAIL}/:serviceName?/:application?`,
                element: <TransactionDetail />,
                loader: transactionDetailRouteLoader,
              },
              {
                path: `${APP_PATH.INSPECTOR}/:serviceName?/:application?`,
                element: <Inspector />,
                loader: inspectorRouteLoader,
              },
              {
                path: `${APP_PATH.THREAD_DUMP}/:serviceName?/:application?`,
                element: <ThreadDump />,
                loader: threadDumpRouteLoader,
              },
              {
                path: `${APP_PATH.OPEN_TELEMETRY_METRIC}/:serviceName?/:application?`,
                element: <OpenTelemetry />,
                loader: openTelemetryRouteLoader,
              },
              {
                element: <ConfigurationOutlet />,
                children: [
                  {
                    path: `${APP_PATH.CONFIG_ALARM}`,
                    element: <Alarm />,
                  },
                  {
                    path: `${APP_PATH.CONFIG_WEBHOOK}`,
                    element: <Webhook />,
                  },
                  {
                    path: `${APP_PATH.CONFIG_GENERAL}`,
                    element: <General />,
                  },
                  {
                    path: `${APP_PATH.CONFIG_EXPERIMENTAL}`,
                    element: <Experimentals />,
                  },
                  {
                    path: `${APP_PATH.CONFIG_HELP}`,
                    element: <Help />,
                  },
                  {
                    path: `${APP_PATH.CONFIG_INSTALLATION}`,
                    element: <Installation />,
                  },
                  {
                    path: `${APP_PATH.CONFIG_USER_GROUP}`,
                    element: <UserGroup />,
                  },
                  {
                    path: `${APP_PATH.CONFIG_USERS}`,
                    element: <Users />,
                  },
                  {
                    path: `${APP_PATH.CONFIG_AGENT_MANAGEMENT}`,
                    element: <AgentManagement />,
                  },
                  {
                    path: `${APP_PATH.CONFIG_AGENT_STATISTIC}`,
                    element: <AgentStatistic />,
                  },
                  {
                    path: `${APP_PATH.CONFIG_SERVICE_SETTING}`,
                    element: <ServiceSetting />,
                  },
                ],
              },
              {
                path: '*',
                element: <NotFound />,
              },
            ],
          },
        ],
      },
    ],
  },
];

// 첫 라우트 로더보다 먼저 등록해야 한다 — `createBrowserRouter`가 만들어지자마자 첫 경로의 로더를
// 돌리고, 로더가 경로에서 serviceName을 읽는다.
registerAppRoutes(routes);

const router = createBrowserRouter(routes, { basename: BASE_PATH });

export default router;
