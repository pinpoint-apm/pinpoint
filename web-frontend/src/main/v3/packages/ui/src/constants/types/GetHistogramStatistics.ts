/* eslint-disable  @typescript-eslint/no-explicit-any */
export namespace GetHistogramStatistics {
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
    nodeKey?: string;
    linkKey?: string;
  }

  export interface Response {
    agentHistogram: AgentHistogram;
    agentResponseStatistics: AgentResponseStatistics;
    agentTimeSeriesHistogram: AgentTimeSeriesHistogram;
    currentServerTime: number;
    histogram: Histogram;
    instanceCount: number;
    instanceErrorCount: number;
    responseStatistics: ResponseStatistics;
    serverList: ServerList;
    timeSeriesHistogram: TimeSeriesHistogram[];
  }

  export interface ServerList {
    [key: string]: ServerInfo;
  }

  export interface ServerInfo {
    name: string;
    status: any;
    linkList: any[];
    instanceList: InstanceList;
  }

  export interface InstanceList {
    [key: string]: InstanceInfo;
  }

  export interface InstanceInfo {
    hasInspector: boolean;
    name: string;
    agentName: string;
    serviceType: string;
    status: Status;
  }

  export interface Status {
    code: number;
    desc: string;
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
  }

  export interface AgentHistogram {
    [key: string]: Histogram;
  }

  export interface AgentResponseStatistics {
    [key: string]: ResponseStatistics;
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

  export interface AgentTimeSeriesHistogram {
    [key: string]: TimeSeriesHistogram[];
  }
}
