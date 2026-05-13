import { GetServerMap } from './GetServerMap';

export namespace TransactionTraceServerMap {
  export interface Parameters {
    agentId: string;
    spanId: string;
    traceId: string;
    focusTimestamp: number;
    useStatisticsAgentState?: boolean;
    linkTraceId?: string;
    linkSpanId?: string;
  }

  export interface Response {
    transactionId: string;
    spanId: number;
    applicationMapData: GetServerMap.ApplicationMapData;
  }
}
