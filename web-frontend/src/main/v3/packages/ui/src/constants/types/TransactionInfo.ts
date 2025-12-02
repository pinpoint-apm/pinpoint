/* eslint-disable  @typescript-eslint/no-explicit-any */
export namespace TransactionInfoType {
  export interface Parameters {
    agentId: string;
    spanId: string;
    traceId: string;
    focusTimestamp: number;
  }

  export interface Response {
    logLinkEnable: boolean;
    logButtonName: string;
    disableButtonMessage: string;
    logPageUrl: string;
    transactionId: string;
    spanId: number;
    completeState: string;
    callStackStart: number;
    callStackEnd: number;
    callStackIndex: CallStackIndex;
    callStack: any[][]; // ['begin', 'end', 'excludeFromTimeline', 'applicationName', 'tab', 'id', 'parentId', 'isMethod', 'hasChild', 'title', 'arguments', 'executeTime', 'gap', 'elapsedTime', 'barWidth', 'executionMilliseconds', 'simpleClassName', 'methodType', 'apiType', 'agent', 'hasException', 'isAuthorized', 'agentName', 'lineNumber', 'location', 'applicationServiceType', 'exceptionChainId']
    loggingTransactionInfo: boolean;
    agentId: string;
    agentName: string;
    uri: string;
    applicationName: string;
    serviceType: string;
    focusCallStackId?: number;
  }

  export interface CallStackIndex {
    begin: number;
    end: number;
    excludeFromTimeline: number;
    applicationName: number;
    tab: number;
    id: number;
    parentId: string;
    isMethod: number;
    hasChild: number;
    title: number;
    arguments: number;
    executeTime: number;
    gap: number;
    elapsedTime: number;
    barWidth: number;
    executionMilliseconds: number;
    simpleClassName: number;
    methodType: number;
    apiType: number;
    agent: number;
    hasException: number;
    isAuthorized: number;
    agentName: number;
    lineNumber: number;
    location: number;
    applicationServiceType: number;
    exceptionChainId: number;
  }

  export type CallStackKeyValueMap = {
    [K in keyof CallStackIndex]: any;
  } & { subRows?: CallStackKeyValueMap[]; attributedAgent?: string };
}
