// import '@pinpoint-fe/ui/src/dist/pinpoint-fe-common-ui.css';
import { createBrowserRouter, redirect } from 'react-router-dom';
import ServerMap from '@pinpoint-fe/web/src/pages/ServerMap';
import Realtime from '@pinpoint-fe/web/src/pages/ServerMap/Realtime';
import ScatterFullScreen from '@pinpoint-fe/web/src/pages/ScatterFullScreen';
import {
  scatterFullScreenLoader,
  scatterFullScreenRealtimeLoader,
} from './loader/scatterFullScreen';
import { serverMapRouteLoader } from './loader/serverMap';
import { realtimeLoader } from './loader/realtime';
import FilteredMap from '@pinpoint-fe/web/src/pages/FilteredMap';
import { BASE_PATH, APP_PATH } from '@pinpoint-fe/ui/src/constants';
import NotFound from '@pinpoint-fe/web/src/pages/NotFound';
import ErrorAnalysis from '@pinpoint-fe/web/src/pages/ErrorAnalysis';
import { errorAnalysisRouteLoader } from './loader/errorAnalysis';
import ApiCheck from '@pinpoint-fe/web/src/pages/ApiCheck';
import { urlStatisticRouteLoader } from './loader/urlStatistic';
import UrlStatistic from '@pinpoint-fe/web/src/pages/UrlStatistic';
import SystemMetric from '@pinpoint-fe/web/src/pages/SystemMetric';
import { systemMetricRouteLoader } from './loader/systemMetric';
import General from '@pinpoint-fe/web/src/pages/config/General';
import Experimentals from '@pinpoint-fe/web/src/pages/config/Experimentals';
import TransactionList from '@pinpoint-fe/web/src/pages/TransactionList';
import TransactionDetail from '@pinpoint-fe/web/src/pages/TransactionDetail';
import { transactionRouteLoader } from './loader/transaction';
import { transactionDetailRouteLoader } from './loader/transactionDetail';
import { inspectorRouteLoader } from './loader/inspector';
import Inspector from '@pinpoint-fe/web/src/pages/Inspector';
import ThreadDump from '@pinpoint-fe/web/src/pages/ThreadDump';
import OpenTelemetry from '@pinpoint-fe/web/src/pages/OpenTelemetry';
import Help from '@pinpoint-fe/web/src/pages/config/Help';
import Installation from '@pinpoint-fe/web/src/pages/config/Installation';
import UserGroup from '@pinpoint-fe/web/src/pages/config/UserGroup';
import Users from '@pinpoint-fe/web/src/pages/config/Users';
import Alarm from '@pinpoint-fe/web/src/pages/config/Alarm';
import Webhook from '@pinpoint-fe/web/src/pages/config/Webhook';
import AgentManagement from '@pinpoint-fe/web/src/pages/config/AgentManagement';
import AgentStatistic from '@pinpoint-fe/web/src/pages/config/AgentStatistic';
import { threadDumpRouteLoader } from './loader/threadDump';
import { openTelemetryRouteLoader } from './loader/openTelemetry';
import { handleV2RouteLoader } from './loader/handleV2';

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
      element: <ScatterFullScreen />,
      loader: scatterFullScreenLoader,
    },
    {
      path: `${APP_PATH.SCATTER_FULL_SCREEN_REALTIME}/:application?`,
      element: <ScatterFullScreen />,
      loader: scatterFullScreenRealtimeLoader,
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
