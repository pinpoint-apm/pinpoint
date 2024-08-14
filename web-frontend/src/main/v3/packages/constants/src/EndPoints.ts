export const LOCAL_API_PATH = '/api';
// export const LOCAL_API_PATH = '/api';

export const CONFIGURATION = `${LOCAL_API_PATH}/configuration`;
export const SERVER_TIME = `${LOCAL_API_PATH}/serverTime`;
// export const = '/userConfiguration';
export const APPLICATION_LIST = `${LOCAL_API_PATH}/applications`;
// export const = '/getServerMapData';
export const SERVER_MAP_DATA_V2 = `${LOCAL_API_PATH}/getServerMapDataV2`;
export const RESPONSE_TIME_HISTOGRAM_DATA_V2 = `${LOCAL_API_PATH}/getResponseTimeHistogramDataV2`;
// export const = '/getLinkTimeHistogramData';
export const FILTERED_SERVER_MAP_DATA = `${LOCAL_API_PATH}/getFilteredServerMapDataMadeOfDotGroup`;
// export const = '/getAgentList';
// export const = '/getAgentStat/jvmGc/chart';
// export const = '/getAgentStat/cpuLoad/chart';
// export const = '/getAgentStat/transaction/chart';
// export const = '/getAgentStat/activeTrace/chart';
// export const = '/getAgentStat/responseTime/chart';
// export const = '/getAgentStat/dataSource/chartList';
// export const = '/getAgentStat/totalThreadCount/chart';
// export const = '/getAgentStat/loadedClass/chart';
// export const = '/getApplicationStat/memory/chart';
// export const = '/getApplicationStat/cpuLoad/chart';
// export const = '/getApplicationStat/transaction/chart';
// export const = '/getApplicationStat/activeTrace/chart';
// export const = '/getApplicationStat/responseTime/chart';
// export const = '/getApplicationStat/dataSource/chart';
// export const = '/getApplicationStat/totalThreadCount/chart';
// export const = '/getApplicationStat/loadedClass/chart';
// export const = '/getAgentStatusTimeline';
// export const = '/getAgentEvents';
// export const = '/getAgentInfo';
// export const = '/transactionmetadata';
export const TRANSACTION_INFO = `${LOCAL_API_PATH}/transactionInfo`;
// export const = '/transactionTimelineInfo';
// export const = '/sqlBind';
// export const = '/jsonBind';
export const SCATTER_DATA = `${LOCAL_API_PATH}/getScatterData`;
export const ACTIVE_THREAD_LIGHT_DUMP = `${LOCAL_API_PATH}/agent/activeThreadLightDump`;
export const ACTIVE_THREAD_DUMP = `${LOCAL_API_PATH}/agent/activeThreadDump`;
// export const = '/getAgentInstallationInfo';
// export const = '/isAvailableApplicationName';
// export const = '/isAvailableAgentId';
export const USER_GROUP = `${LOCAL_API_PATH}/userGroup`;
// export const = '/user';
// export const = '/userGroup/member';
export const ALARM_RULE_CHECKER = `${LOCAL_API_PATH}/application/alarmRule/checker`;
export const ALARM_RULE = `${LOCAL_API_PATH}/application/alarmRule`;
// export const = '/getAgentStat/fileDescriptor/chart';
// export const = '/getApplicationStat/fileDescriptor/chart';
// export const = '/getAgentStat/directBuffer/chart';
// export const = '/getApplicationStat/directBuffer/chart';
// export const = '/admin/removeAgentId';
// export const = '/admin/removeInactiveAgents';
export const BIND = `${LOCAL_API_PATH}/bind`;
// export const = '/getAgentStat/uriStat/chartList';
export const HEATMAP_DRAG = `${LOCAL_API_PATH}/heatmap/drag`;
export const TRACE_VIEWER_DATA = `${LOCAL_API_PATH}/traceViewerData`;
export const WEBHOOK = `${LOCAL_API_PATH}/application/webhook`;
// export const = '/application/webhookSendInfo';
export const INCLUDE_WEBHOOK = `${LOCAL_API_PATH}/application/alarmRule/includeWebhooks`;
//   '/getApdexScore',
export const APDEX_SCORE = `${LOCAL_API_PATH}/getApdexScore`;
//   '/getApplicationStat/apdexScore/chart',
//   '/getAgentStat/apdexScore/chart',
//   '/agents/search-all',
export const SEARCH_APPLICATION = `${LOCAL_API_PATH}/agents/search-application`;
//   '/agents/statistics',
export const ERROR_ANALYSIS_GROUPS = `${LOCAL_API_PATH}/errors/groups`;
export const ERROR_ANALYSIS_ERROR_LIST = `${LOCAL_API_PATH}/errors/errorList`;
export const ERROR_ANALYSIS_GROUPED_ERROR_LIST = `${LOCAL_API_PATH}/errors/errorList/groupBy`;
export const ERROR_ANALYSIS_CHART = `${LOCAL_API_PATH}/errors/chart`;
export const ERROR_ANALYSIS_TRANSACTION_INFO = `${LOCAL_API_PATH}/errors/transactionInfo`;

export const URL_STATISTIC_SUMMARY = `${LOCAL_API_PATH}/uriStat/summary`;
export const URL_STATISTIC_CHART = `${LOCAL_API_PATH}/uriStat/chart`;

export const SYSTEM_METRIC_HOST_GROUP = `${LOCAL_API_PATH}/systemMetric/hostGroup`;
export const SYSTEM_METRIC_HOST = `${LOCAL_API_PATH}/systemMetric/hostGroup/host`;
export const SYSTEM_METRIC_METRIC_INFO = `${LOCAL_API_PATH}/systemMetric/hostGroup/host/collectedMetricInfoV2`;
export const SYSTEM_METRIC_TAGS = `${LOCAL_API_PATH}/systemMetric/hostGroup/host/collectedTags`;
export const SYSTEM_METRIC_CHART = `${LOCAL_API_PATH}/systemMetric/hostGroup/host/collectedMetricData`;

export const INSPECTOR_AGENT_CHART = `${LOCAL_API_PATH}/inspector/agentStat/chart`;
export const INSPECTOR_AGENT_DATA_SOURCE_CHART = `${LOCAL_API_PATH}/inspector/agentStat/chartList`;
export const INSPECTOR_AGENT_EVENTS = `${LOCAL_API_PATH}/getAgentEvents`;
export const INSPECTOR_AGENT_INFO = `${LOCAL_API_PATH}/getDetailedAgentInfo`;
export const INSPECTOR_AGENT_STATUS_TIMELINE = `${LOCAL_API_PATH}/getAgentStatusTimeline`;
export const INSPECTOR_APPLICATION_CHART = `${LOCAL_API_PATH}/inspector/applicationStat/chart`;
export const INSPECTOR_APPLICATION_DATA_SOURCE_CHART = `${LOCAL_API_PATH}/inspector/applicationStat/chartList`;

export const CONFIG_APPLICATION_DUPLICATION_CHECK = `${LOCAL_API_PATH}/isAvailableApplicationName`;
export const CONFIG_AGENT_DUPLICATION_CHECK = `${LOCAL_API_PATH}/isAvailableAgentId`;
export const CONFIG_INSTALLATION_INFO = `${LOCAL_API_PATH}/getAgentInstallationInfo`;
export const CONFIG_USER_GROUP = `${LOCAL_API_PATH}/userGroup`;
export const CONFIG_GROUP_MEMBER = `${LOCAL_API_PATH}/userGroup/member`;
export const CONFIG_USERS = `${LOCAL_API_PATH}/user`;

export const OTLP_METRIC_DEF_PROPERTY = `${LOCAL_API_PATH}/otlp/metricDef/property`;
export const OTLP_METRIC_DEF_USER_DEFINED = `${LOCAL_API_PATH}/otlp/metricDef/userDefined`;
