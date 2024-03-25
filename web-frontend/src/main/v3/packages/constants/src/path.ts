export const BASE_PATH = process.env.BASE_PATH || '';
export const IMAGE_PATH = `${BASE_PATH}/img`;
export const APP_PATH = {
  API_CHECK: '/apiCheck',
  CONFIG_ALARM: '/config/alarm',
  CONFIG_ESM: '/config/esm',
  CONFIG_EXPERIMENTAL: '/config/experimental',
  CONFIG_GENERAL: '/config/general',
  CONFIG_HELP: '/config/help',
  CONFIG_INSTALLATION: '/config/installation',
  CONFIG_USER_GROUP: '/config/userGroup',
  CONFIG_USERS: '/config/users',
  CONFIG_WEBHOOK: '/config/webhook',
  CONFIG: '/config',
  ERROR_ANALYSIS: '/errorAnalysis',
  FILTERED_MAP: '/filteredMap',
  INSPECTOR: '/inspector',
  SCATTER_FULL_SCREEN_REALTIME: '/scatterFullScreenMode/realtime',
  SCATTER_FULL_SCREEN: '/scatterFullScreenMode',
  SERVER_MAP_REALTIME: '/serverMap/realtime',
  SERVER_MAP: '/serverMap',
  SQL_STATISTIC: '/sqlStatistic',
  SYSTEM_METRIC: '/systemMetric',
  TRANSACTION_DETAIL: '/transactionDetail',
  TRANSACTION_LIST: '/transactionList',
  URL_STATISTIC: '/urlStatistic',
};

export const ADMIN_PATH = [APP_PATH.CONFIG_USERS];
