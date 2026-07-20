export type Transaction = {
  exception: number;
  agentId: string;
  spanId: string;
  elapsed: number;
  endpoint: string;
  collectorAcceptTime: number;
  application: string;
  agentName: null;
  remoteAddr: string;
  startTime: number;
  traceId: string;
  // present on /api/transaction/metadata and /api/transactionmetadata responses;
  // absent on /api/heatmap/drag rows
  applicationName?: string;
  serviceType?: string;
};
