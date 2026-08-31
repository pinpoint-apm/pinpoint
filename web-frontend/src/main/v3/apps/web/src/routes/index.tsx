import { lazy } from 'react';
import { createBrowserRouter, redirect } from 'react-router';
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
const ServiceAlarm = lazy(() => import('@pinpoint-fe/web/src/pages/config/ServiceAlarm'));

const defaultLoader = () => {
  return redirect('/serverMap');
};

const router = createBrowserRouter(
  [
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
                  path: `${APP_PATH.SCATTER_FULL_SCREEN}/:application?`,
                  element: <ScatterOrHeatmapFullScreen />,
                  loader: scatterOrHeatmapFullScreenLoader,
                },
                {
                  path: `${APP_PATH.SCATTER_FULL_SCREEN_REALTIME}/:application?`,
                  element: <ScatterOrHeatmapFullScreen />,
                  loader: scatterOrHeatmapFullScreenRealtimeLoader,
                },
                {
                  path: `${APP_PATH.HEATMAP_FULL_SCREEN}/:application?`,
                  element: <ScatterOrHeatmapFullScreen />,
                  loader: scatterOrHeatmapFullScreenLoader,
                },
                {
                  path: `${APP_PATH.HEATMAP_FULL_SCREEN_REALTIME}/:application?`,
                  element: <ScatterOrHeatmapFullScreen />,
                  loader: scatterOrHeatmapFullScreenRealtimeLoader,
                },
                {
                  path: `${APP_PATH.ERROR_ANALYSIS}/:application?`,
                  element: <ErrorAnalysis />,
                  loader: errorAnalysisRouteLoader,
                },
                {
                  path: `${APP_PATH.URL_STATISTIC}/:application?`,
                  element: <UrlStatistic />,
                  loader: urlStatisticRouteLoader,
                },
                {
                  path: `${APP_PATH.SYSTEM_METRIC}/:hostGroup?`,
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
                  path: `${APP_PATH.INSPECTOR}/:application?`,
                  element: <Inspector />,
                  loader: inspectorRouteLoader,
                },
                {
                  path: `${APP_PATH.THREAD_DUMP}/:application?`,
                  element: <ThreadDump />,
                  loader: threadDumpRouteLoader,
                },
                {
                  path: `${APP_PATH.OPEN_TELEMETRY_METRIC}/:application?`,
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
                    {
                      path: `${APP_PATH.CONFIG_SERVICE_ALARM}`,
                      element: <ServiceAlarm />,
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
  ],
  { basename: BASE_PATH },
);

export default router;
