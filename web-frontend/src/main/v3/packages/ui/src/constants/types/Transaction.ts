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
};
