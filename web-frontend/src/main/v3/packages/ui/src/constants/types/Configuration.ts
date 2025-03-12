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
  'periodMax.exceptionTrace': number;
  'periodInterval.exceptionTrace': string[];
  'periodMax.inspector': number;
  'periodInterval.inspector': string[];
  'periodMax.otlpMetric': number;
  'periodInterval.otlpMetric': string[];
  'periodMax.serverMap': number;
  'periodInterval.serverMap': string[];
  'periodMax.systemMetric': number;
  'periodInterval.systemMetric': string[];
  'periodMax.uriStat': number;
  'periodInterval.uriStat': string[];
}
