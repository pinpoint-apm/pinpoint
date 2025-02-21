/* eslint-disable  @typescript-eslint/no-explicit-any */
export namespace TransactionInfoType {
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
    callStackStart: number;
    callStackEnd: number;
    callStackIndex: CallStackIndex;
    callStack: any[][]; // ['depth', 'begin', 'end', 'excludeFromTimeline', 'applicationName', 'tab', 'id', 'parentId', 'isMethod', 'hasChild', 'title', 'arguments', 'executeTime', 'gap', 'elapsedTime', 'barWidth', 'executionMilliseconds', 'simpleClassName', 'methodType', 'apiType', 'agent', 'isFocused', 'hasException', 'isAuthorized', 'agentName', 'lineNumber', 'location', 'applicationServiceType', 'exceptionChainId']
    applicationMapData: ApplicationMapData;
    loggingTransactionInfo: boolean;
    agentId: string;
    agentName: string;
    applicationName: string;
    applicationId: string;
  }

  export interface CallStackIndex {
    depth: number;
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
    isFocused: number;
    hasException: number;
    isAuthorized: number;
    agentName: number;
    lineNumber: number;
    location: number;
    applicationServiceType: number;
    exceptionChainId: number;
  }

  export interface ApplicationMapData {
    nodeDataArray: NodeDataArray[];
    linkDataArray: LinkDataArray[];
  }

  export interface NodeDataArray {
    key: string;
    applicationName: string;
    category: string;
    serviceType: string;
    serviceTypeCode: number;
    isWas: boolean;
    isQueue: boolean;
    isAuthorized: boolean;
    totalCount: number;
    errorCount: number;
    slowCount: number;
    hasAlert: boolean;
    responseStatistics: ResponseStatistics;
    histogram: Histogram;
    apdexScore: number;
    apdexFormula: ApdexFormula;
    agentHistogram: AgentHistogram;
    agentResponseStatistics: AgentResponseStatistics;
    timeSeriesHistogram: TimeSeriesHistogram[];
    agentTimeSeriesHistogram: AgentTimeSeriesHistogram;
    instanceCount: number;
    instanceErrorCount: number;
    agentIds: string[];
    agentIdNameMap: AgentIdNameMap;
    serverList: ServerList;
  }

  export interface ResponseStatistics {
    Tot: number;
    Sum: number;
    Avg: number;
    Max: number;
  }

  export interface Histogram {
    '1s'?: number;
    '3s'?: number;
    '5s'?: number;
    Slow: number;
    Error: number;
    '100ms'?: number;
    '300ms'?: number;
    '500ms'?: number;
  }

  export interface ApdexFormula {
    satisfiedCount: number;
    toleratingCount: number;
    totalSamples: number;
  }

  export interface AgentHistogram {
    [key: string]: Histogram;
  }

  export interface AgentResponseStatistics {
    [key: string]: ResponseStatistics;
  }

  export interface TimeSeriesHistogram {
    key: string;
    values: number[][];
  }

  export interface AgentTimeSeriesHistogram {
    [key: string]: TimeSeriesHistogram;
  }

  export interface AgentIdNameMap {
    [key: string]: any;
  }

  export interface ServerList {
    [key: string]: {
      name: string;
      status: any;
      linkList: any[];
      instanceList: InstanceList;
    };
  }

  export interface InstanceList {
    [key: string]: {
      hasInspector: boolean;
      name: string;
      agentName: any;
      serviceType: string;
      status: Status;
    };
  }

  export interface Status {
    code: number;
    desc: string;
  }

  export interface LinkDataArray {
    key: string;
    from: string;
    to: string;
    fromAgent?: any[];
    fromAgentIdNameMap?: FromAgentIdNameMap;
    sourceInfo: SourceInfo;
    targetInfo: TargetInfo;
    filterApplicationName: string;
    filterApplicationServiceTypeCode: number;
    filterApplicationServiceTypeName: string;
    totalCount: number;
    errorCount: number;
    slowCount: number;
    responseStatistics: ResponseStatistics;
    histogram: Histogram;
    timeSeriesHistogram: TimeSeriesHistogram[];
    sourceHistogram: SourceHistogram;
    targetHistogram: TargetHistogram;
    sourceTimeSeriesHistogram: SourceTimeSeriesHistogram;
    sourceResponseStatistics: SourceResponseStatistics;
    targetResponseStatistics: TargetResponseStatistics;
    hasAlert: boolean;
    toAgent?: any[];
    toAgentIdNameMap?: ToAgentIdNameMap;
  }

  export interface SourceInfo {
    applicationName: string;
    serviceType: string;
    serviceTypeCode: number;
    isWas: boolean;
  }

  export interface TargetInfo {
    applicationName: string;
    serviceType: string;
    serviceTypeCode: number;
    isWas: boolean;
  }

  export interface ResponseStatistics {
    Tot: number;
    Sum: number;
    Avg: number;
    Max: number;
  }

  export interface TimeSeriesHistogram {
    key: string;
    values: number[][];
  }

  export interface SourceHistogram {
    [key: string]: Histogram;
  }

  export interface TargetHistogram {
    [key: string]: Histogram;
  }

  export interface SourceTimeSeriesHistogram {
    [key: string]: TimeSeriesHistogram[];
  }

  export interface SourceResponseStatistics {
    [key: string]: ResponseStatistics;
  }

  export interface TargetResponseStatistics {
    [key: string]: ResponseStatistics;
  }

  export interface ToAgentIdNameMap {}

  export interface FromAgentIdNameMap {}

  export type CallStackKeyValueMap = {
    [K in keyof CallStackIndex]: any;
  } & { subRows?: CallStackKeyValueMap[]; attributedAgent?: string };
}
