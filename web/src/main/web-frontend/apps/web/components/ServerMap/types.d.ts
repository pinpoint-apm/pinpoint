interface ServerMapInfo {
  applicationMapData: {
    range: QueryRange;
    nodeDataArray: NodeInfo[];
    linkDataArray: LinkInfo[];
  };
}

interface QueryRange {
  from: number;
  to: number;
  toDateTime: string;
  fromDateTime: string;
  range: number;
}

interface LinkInfo {
  errorCount: number;
  filterApplicationName?: string;
  filterApplicationServiceTypeCode?: number;
  filterApplicationServiceTypeName?: string;
  filterTargetRpcList?: any[];
  from: string;
  fromAgent?: string[];
  fromAgentIdNameMap?: { [key: string]: string }
  hasAlert: boolean;
  histogram?: ResponseTime | ResponseMilliSecondTime;
  responseStatistics?: ResponseStatistics;
  key: string;
  slowCount: number;
  sourceHistogram?: { [key: string]: ResponseTime | ResponseMilliSecondTime };
  sourceResponseStatistics?: { [key: string]: ResponseStatistics };
  sourceInfo: SourceInfo;
  sourceTimeSeriesHistogram?: { [key: string]: Histogram }[];
  targetHistogram?: { [key: string]: ResponseTime | ResponseMilliSecondTime };
  targetResponseStatistics?: { [key: string]: ResponseStatistics };
  targetInfo: SourceInfo;
  timeSeriesHistogram?: Histogram[];
  to: string;
  toAgent?: string[];
  toAgentIdNameMap?: { [key: string]: string }
  totalCount: number;
  isMerged?: boolean;
  isFiltered?: boolean;
}

interface NodeInfo {
  agentHistogram?: { [key: string]: ResponseTime | ResponseMilliSecondTime }[];
  agentTimeSeriesHistogram?: { [key: string]: Histogram[] };
  agentIds?: string[];
  agentIdNameMap?: { [key: string]: string };
  apdexScore?: number;
  applicationName: string;
  category: string;
  errorCount?: number;
  hasAlert?: boolean;
  histogram?: ResponseTime | ResponseMilliSecondTime;
  responseStatistics?: ResponseStatistics;
  agentResponseStatistics?: { [key: string]: ResponseStatistics };
  instanceCount: number;
  instanceErrorCount?: number;
  isAuthorized: boolean;
  isQueue?: boolean;
  isWas?: boolean;
  key: string;
  serverList?: { [key: string]: ServerInfo };
  serviceType: string;
  serviceTypeCode?: string;
  slowCount?: number;
  timeSeriesHistogram?: Histogram[];
  totalCount?: number;
  isMerged?: boolean;
  mergedNodes?: any[];
  topCountNodes?: any[];
  mergedSourceNodes?: any[];
}

interface SourceInfo {
  applicationName: string;
  code: number;
  serviceType?: string;
  serviceTypeCode: number;
  isWas: boolean;
}

interface Histogram {
  key: string;
  values: number[][];
}

interface ResponseTime {
  '1s': number;
  '3s': number;
  '5s': number;
  'Slow': number;
  'Error': number;
  [key: string]: number;
}

interface ResponseMilliSecondTime {
  '100ms': number;
  '300ms': number;
  '500ms': number;
  'Error': number;
  'Slow': number;
  [key: string]: number;
}

interface ResponseStatistics {
  'Avg': number;
  'Max': number;
  'Sum': number;
  'Tot': number;
}

interface InstanceInfo {
  hasInspector: boolean;
  name: string;
  serviceType: string;
  status: InstanceStatus;
}

interface InstanceStatus {
  code: number;
  desc: string;
}

interface ServerInfo {
  instanceList: { [key: string]: InstanceInfo };
  name: string;
  status: any; // 응답 형식을 아직 확인 못함.
}
