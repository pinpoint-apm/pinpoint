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
const Experimentals = lazy(() => import('@pinpoint-fe/web/src/pages/config/Experimentals'));
const TransactionList = lazy(() => import('@pinpoint-fe/web/src/pages/TransactionList'));
const TransactionDetail = lazy(() => import('@pinpoint-fe/web/src/pages/TransactionDetail'));
const Inspector = lazy(() => import('@pinpoint-fe/web/src/pages/Inspector'));
const ThreadDump = lazy(() => import('@pinpoint-fe/web/src/pages/ThreadDump'));
const OpenTelemetry = lazy(() => import('@pinpoint-fe/web/src/pages/OpenTelemetry'));
// SubMenu인 page를 Lazy import 시 네비게이션이 지연되면서 DropdownMenu가 닫히는 시점도 지연되어
// SubMenu클릭 -> 흰 바탕에 DropdownMenu가 닫히지 않고 보여지는 이슈가 있음
// Config 페이지들은 Lazy import를 제거하여 컴포넌트를 미리 로드하여 DropdownMenu가 즉시 닫히도록 함
import General from '@pinpoint-fe/web/src/pages/config/General';
import Help from '@pinpoint-fe/web/src/pages/config/Help';
import Installation from '@pinpoint-fe/web/src/pages/config/Installation';
import UserGroup from '@pinpoint-fe/web/src/pages/config/UserGroup';
import Users from '@pinpoint-fe/web/src/pages/config/Users';
import Alarm from '@pinpoint-fe/web/src/pages/config/Alarm';
import Webhook from '@pinpoint-fe/web/src/pages/config/Webhook';
import AgentManagement from '@pinpoint-fe/web/src/pages/config/AgentManagement';
import AgentStatistic from '@pinpoint-fe/web/src/pages/config/AgentStatistic';

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
