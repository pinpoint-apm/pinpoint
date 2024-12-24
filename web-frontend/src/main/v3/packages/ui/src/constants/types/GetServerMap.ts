/* eslint-disable  @typescript-eslint/no-explicit-any */
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
    linkDataArray: LinkData[];
    nodeDataArray: NodeData[];
  }

  export interface Range {
    from: number;
    to: number;
    fromDateTime: string;
    toDateTime: string;
  }

  export interface LinkData {
    key: string;
    from: string;
    to: string;
    fromAgent?: string[];
    toAgent?: string[];
    fromAgentIdNameMap?: FromAgentIdNameMap;
    toAgentIdNameMap?: ToAgentIdNameMap;
    sourceInfo: SourceInfo;
    targetInfo: TargetInfo;
    filterApplicationName: string;
    filterApplicationServiceTypeCode: number;
    filterApplicationServiceTypeName: string;
    filterTargetRpcList?: FilterTargetRpcList[];
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
    isWas: boolean;
  }

  export interface TargetInfo {
    applicationName: string;
    serviceType: string;
    serviceTypeCode: number;
    isWas: boolean;
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
    values: number[][];
  }

  export interface NodeData {
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
    timeSeriesHistogram: TimeSeriesHistogram[];
    instanceCount: number;
    instanceErrorCount: number;
    agentIds: string[];
    agentIdNameMap: AgentIdNameMap;
    isMerged?: boolean;
    mergedNodes?: any[];
    topCountNodes?: any[];
    mergedSourceNodes?: any[];
  }

  export interface ApdexFormula {
    satisfiedCount: number;
    toleratingCount: number;
    totalSamples: number;
  }

  export interface AgentIdNameMap {
    [key: string]: string | null;
  }
}
