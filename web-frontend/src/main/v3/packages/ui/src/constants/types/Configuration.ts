export interface Configuration {
  webhookEnable: boolean;
  showActiveThread: boolean;
  showActiveThreadDump: boolean;
  sendUsage: boolean;
  editUserInfo: boolean;
  enableServerMapRealTime: boolean;
  showApplicationStat: boolean;
  showStackTraceOnError: boolean;
  showSystemMetric: boolean;
  showUrlStat: boolean;
  showExceptionTrace: boolean;
  showOtlpMetric: boolean;
  openSource: boolean;
  version: string;
  'experimental.enableServerMapRealTime.value': boolean;
  'experimental.enableServerSideScanForScatter.description': string;
  'experimental.useStatisticsAgentState.value': boolean;
  'experimental.sampleScatter.description': string;
  'experimental.useStatisticsAgentState.description': string;
  'experimental.enableServerMapRealTime.description': string;
  'experimental.enableServerSideScanForScatter.value': boolean;
  'experimental.sampleScatter.value': boolean;
}
