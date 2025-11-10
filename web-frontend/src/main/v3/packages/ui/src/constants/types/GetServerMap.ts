/* eslint-disable  @typescript-eslint/no-explicit-any */
// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace GetServerMap {
  export interface Response {
    applicationMapData: ApplicationMapData;
  }

  export interface Parameters {
    applicationName: string;
    serviceTypeName: string;
    from: number;
    to: number;
    calleeRange: number;
    callerRange: number;
    wasOnly: boolean;
    bidirectional: boolean;
    useStatisticsAgentState: boolean;
  }

  export interface ApplicationMapData {
    range: Range;
    timestamp: number[];
    linkDataArray: LinkData[];
    nodeDataArray: NodeData[];
  }

  export interface Range {
    from: number;
    to: number;
    fromDateTime: string;
    toDateTime: string;
  }

  export interface Agent {
    id: string;
    name: string;
  }

  export interface LinkData {
    key: string;
    linkKey?: string;
    from: string;
    to: string;
    fromAgents?: Agent[];
    toAgents?: Agent[];
    sourceInfo: SourceInfo;
    targetInfo: TargetInfo;
    filter: {
      applicationName: string;
      serviceTypeCode: number;
      serviceTypeName: string;
      outRpcList?: FilterTargetRpcList[];
    };
    totalCount: number;
    errorCount: number;
    slowCount: number;
    responseStatistics: ResponseStatistics;
    histogram: Histogram;
    timeSeriesHistogram: TimeSeriesHistogram[];
    hasAlert: boolean;
  }

  export interface FromAgentIdNameMap {
    [key: string]: string;
  }

  export interface ToAgentIdNameMap {
    [key: string]: string;
  }

  export interface SourceInfo {
    applicationName: string;
    serviceType: string;
    serviceTypeCode: number;
    nodeCategory: NodeCategory;
  }

  export interface TargetInfo {
    applicationName: string;
    serviceType: string;
    serviceTypeCode: number;
    nodeCategory: NodeCategory;
  }

  export interface FilterTargetRpcList {
    rpc: string;
    rpcServiceTypeCode: number;
  }

  export interface ResponseStatistics {
    Tot: number;
    Sum: number;
    Avg: number;
    Max: number;
  }

  export interface Histogram {
    '1s': number;
    '3s': number;
    '5s': number;
    Slow: number;
    Error: number;
    [key: string]: number;
  }

  export interface TimeSeriesHistogram {
    key: string;
    values: number[];
  }

  export interface NodeData {
    key: string;
    nodeKey?: string;
    applicationName: string;
    serviceType: string;
    serviceTypeCode: number;
    isAuthorized: boolean;
    totalCount: number;
    errorCount: number;
    slowCount: number;
    hasAlert: boolean;
    responseStatistics: ResponseStatistics;
    histogram: Histogram;
    apdex: {
      apdexScore: number;
      apdexFormula: ApdexFormula;
    };
    timeSeriesHistogram: TimeSeriesHistogram[];
    instanceCount: number;
    instanceErrorCount: number;
    agents: Agent[];
    isMerged?: boolean;
    mergedNodes?: any[];
    topCountNodes?: any[];
    mergedSourceNodes?: any[];
    nodeCategory: NodeCategory;
  }

  export interface ApdexFormula {
    satisfiedCount: number;
    toleratingCount: number;
    totalSamples: number;
  }

  export interface AgentIdNameMap {
    [key: string]: string | null;
  }

  export enum NodeCategory {
    UNDEFINED = 'UNDEFINED', //((byte) -1)
    USER = 'USER', //((byte) -10)
    UNKNOWN = 'UNKNOWN', //((byte) -20)
    SERVER = 'SERVER', //((byte) 10)
    DATABASE = 'DATABASE', //((byte) 20)
    MESSAGE_BROKER = 'MESSAGE_BROKER', //((byte) 30)
    CACHE = 'CACHE', //((byte) 40)
  }
}
