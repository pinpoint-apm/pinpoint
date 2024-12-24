export const BASE_PATH = process.env.BASE_PATH || '';
export const IMAGE_PATH = `${BASE_PATH}/img`;
export const APP_PATH = {
  API_CHECK: '/apiCheck',
  CONFIG_ALARM: '/config/alarm',
  CONFIG_EXPERIMENTAL: '/config/experimental',
  CONFIG_GENERAL: '/config/general',
  CONFIG_HELP: '/config/help',
  CONFIG_INSTALLATION: '/config/installation',
  CONFIG_USER_GROUP: '/config/userGroup',
  CONFIG_USERS: '/config/users',
  CONFIG_WEBHOOK: '/config/webhook',
  CONFIG_AGENT_MANAGEMENT: '/config/agentManagement',
  CONFIG_AGENT_STATISTIC: '/config/agentStatistic',
  CONFIG: '/config',
  ERROR_ANALYSIS: '/errorAnalysis',
  FILTERED_MAP: '/filteredMap',
  INSPECTOR: '/inspector',
  OPEN_TELEMETRY_METRIC: '/openTelemetryMetric',
  SCATTER_FULL_SCREEN_REALTIME: '/scatterFullScreenMode/realtime',
  SCATTER_FULL_SCREEN: '/scatterFullScreenMode',
  SERVER_MAP_REALTIME: '/serverMap/realtime',
  SERVER_MAP: '/serverMap',
  SYSTEM_METRIC: '/systemMetric',
  THREAD_DUMP: '/threadDump',
  TRANSACTION_DETAIL: '/transactionDetail',
  TRANSACTION_LIST: '/transactionList',
  URL_STATISTIC: '/urlStatistic',
};

export const ADMIN_PATH = [APP_PATH.CONFIG_USERS];
