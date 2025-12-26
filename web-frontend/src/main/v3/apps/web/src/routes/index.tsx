// import '@pinpoint-fe/ui/src/dist/pinpoint-fe-common-ui.css';
import { lazy } from 'react';
import { createBrowserRouter, redirect } from 'react-router-dom';
import {
  serverMapRouteLoader,
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

import ServerMap from '@pinpoint-fe/web/src/pages/ServerMap';
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
const General = lazy(() => import('@pinpoint-fe/web/src/pages/config/General'));
const Experimentals = lazy(() => import('@pinpoint-fe/web/src/pages/config/Experimentals'));
const TransactionList = lazy(() => import('@pinpoint-fe/web/src/pages/TransactionList'));
const TransactionDetail = lazy(() => import('@pinpoint-fe/web/src/pages/TransactionDetail'));
const Inspector = lazy(() => import('@pinpoint-fe/web/src/pages/Inspector'));
const ThreadDump = lazy(() => import('@pinpoint-fe/web/src/pages/ThreadDump'));
const OpenTelemetry = lazy(() => import('@pinpoint-fe/web/src/pages/OpenTelemetry'));
const Help = lazy(() => import('@pinpoint-fe/web/src/pages/config/Help'));
const Installation = lazy(() => import('@pinpoint-fe/web/src/pages/config/Installation'));
const UserGroup = lazy(() => import('@pinpoint-fe/web/src/pages/config/UserGroup'));
const Users = lazy(() => import('@pinpoint-fe/web/src/pages/config/Users'));
const Alarm = lazy(() => import('@pinpoint-fe/web/src/pages/config/Alarm'));
const Webhook = lazy(() => import('@pinpoint-fe/web/src/pages/config/Webhook'));
const AgentManagement = lazy(() => import('@pinpoint-fe/web/src/pages/config/AgentManagement'));
const AgentStatistic = lazy(() => import('@pinpoint-fe/web/src/pages/config/AgentStatistic'));

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
      path: `${APP_PATH.API_CHECK}`,
      element: <ApiCheck />,
      // loader: defaultLoader,
    },
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
      path: `${APP_PATH.FILTERED_MAP}/:application?`,
      element: <FilteredMap />,
      loader: serverMapRouteLoader,
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
      path: `${APP_PATH.TRANSACTION_LIST}/:application?`,
      element: <TransactionList />,
      loader: transactionRouteLoader,
    },
    {
      path: `${APP_PATH.TRANSACTION_DETAIL}/:application?`,
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
      path: '*',
      element: <NotFound />,
    },
  ],
  { basename: BASE_PATH },
);

export default router;
