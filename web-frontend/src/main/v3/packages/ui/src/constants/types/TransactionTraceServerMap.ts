import { GetServerMap } from './GetServerMap';

export namespace TransactionTraceServerMap {
  export interface Parameters {
    agentId: string;
    spanId: string;
    traceId: string;
    focusTimestamp: number;
    useStatisticsAgentState?: boolean;
  }

  export interface Response {
    logLinkEnable: boolean;
    logButtonName: string;
    disableButtonMessage: string;
    logPageUrl: string;
    transactionId: string;
    spanId: number;
    completeState: string;
    loggingTransactionInfo: boolean;
    focusCallStackId?: number;
    callStackStart: number;
    callStackEnd: number;
    applicationMapData: GetServerMap.ApplicationMapData;
    agentId: string;
    applicationName: string;
    agentName: string;
    uri: string;
  }
}
