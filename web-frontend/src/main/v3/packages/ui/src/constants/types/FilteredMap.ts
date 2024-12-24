/* eslint-disable  @typescript-eslint/no-explicit-any */
export namespace FilteredMapType {
  export interface FilterState {
    fromApplication?: SearchParameters['fa']; // from applicationName
    fromServiceType?: SearchParameters['fst']; // from serviceType
    fromAgents?: string[];
    toApplication?: SearchParameters['ta']; // to applicationName
    toServiceType?: SearchParameters['tst']; // to serviceType
    toAgents?: string[];
    transactionResult: SearchParameters['ie']; // include exception (X)
    // if single node
    applicationName?: SearchParameters['a']; //  applicationName
    serviceType?: SearchParameters['st']; // serviceType
    agentName?: SearchParameters['an']; // agentName
    agents?: string[];
    // settings by user
    responseFrom?: SearchParameters['rf']; // responseFrom
    responseTo?: SearchParameters['rt']; // responseTo
    url?: SearchParameters['url']; // request url pattern
    fromAgentName?: SearchParameters['fan']; // from agent name
    toAgentName?: SearchParameters['tan']; // to agent name
    // hint
    hint?: Hint;
  }

  export interface SearchParameters {
    fa?: string; // from applicationName
    fst?: string; // from serviceType
    ta?: string; // to applicationName
    tst?: string; // to serviceType
    ie: boolean | null; // include exception (X)
    // if single node
    a?: string; //  applicationName
    st?: string; // serviceType
    an?: string; // agentName
    // settings by user
    rf?: number; // responseFrom
    rt?: number | string; // responseTo
    url?: string; // request url pattern
    fan?: string; // from agent name
    tan?: string; // to agent name
  }

  export interface Parameters {
    applicationName: string;
    serviceTypeName: string;
    from: number;
    to: number;
    originTo: number;
    calleeRange: number;
    callerRange: number;
    filter: string;
    hint: string;
    v: number;
    limit: number;
    xGroupUnit: number;
    yGroupUnit: number;
    useStatisticsAgentState: boolean;
  }

  export interface Hint {
    [key: string]: string[];
  }

  export interface Response {
    applicationMapData: ApplicationMapData;
    lastFetchedTimestamp: number;
    applicationScatterData: ApplicationScatterData;
  }

  export interface ApplicationScatterData {
    [key: string]: ScatterData;
  }

  export interface ScatterData {
    from: number;
    to: number;
    resultFrom: number;
    resultTo: number;
    scatter: Scatter;
  }

  export interface Scatter {
    metadata: Metadata;
    dotList: number[][];
  }

  export interface Metadata {
    [key: number]: [string, string, number];
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
    sourceHistogram: SourceHistogram;
    targetHistogram: TargetHistogram;
    sourceTimeSeriesHistogram: SourceTimeSeriesHistogram;
    sourceResponseStatistics: SourceResponseStatistics;
    targetResponseStatistics: TargetResponseStatistics;
    hasAlert: boolean;
  }

  export interface FromAgentIdNameMap {
    [key: string]: string;
  }

  export interface ToAgentIdNameMap {
    [key: string]: string | null;
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
    rpc?: string;
    rpcServiceTypeCode?: number;
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

  export interface AgentTimeSeriesHistogram {
    [key: string]: TimeSeriesHistogram[];
  }

  export interface AgentIdNameMap {
    [key: string]: string | any;
  }

  export interface ServerList {
    [key: string]: ServerInfo;
  }

  export interface ServerInfo {
    name: string;
    status: any;
    linkList: LinkList[];
    instanceList: InstanceList;
  }

  export interface InstanceList {
    [key: string]: {
      hasInspector: boolean;
      name: string;
      agentName: string | null;
      serviceType: string;
      status: Status;
    };
  }

  export interface LinkList {
    linkName: string;
    linkURL: string;
    linkType: string;
  }

  export interface Status {
    code: number;
    desc: string;
  }
}
