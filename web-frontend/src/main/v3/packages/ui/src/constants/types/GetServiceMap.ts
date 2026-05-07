import { GetServerMap } from './GetServerMap';

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace GetServiceMap {
  export interface Parameters {
    applicationName: string;
    serviceTypeName?: string;
    serviceTypeCode?: number;
    from: number | string;
    to: number | string;
    calleeRange?: number;
    callerRange?: number;
    wasOnly?: boolean;
    bidirectional?: boolean;
    useStatisticsAgentState?: boolean;
    keepServiceNames?: string[];
  }

  export interface Response {
    applicationMapData: ApplicationMapData;
  }

  export interface ApplicationMapData {
    range: GetServerMap.Range;
    timestamp: number[];
    nodeDataArray: NodeEntry[];
    linkDataArray: LinkEntry[];
  }

  export type NodeEntry = AppNode | ServiceGroupNode;
  export type LinkEntry = AppLink | ServiceGroupLink;

  export interface AppNode extends GetServerMap.NodeData {
    type: 'app';
    serviceKey: string;
    serviceName: string;
    isQueue: boolean;
  }

  export interface ServiceGroupNode {
    key: string;
    type: 'service';
    serviceName: string;
    nodes: AppNode[];
  }

  export interface AppLink extends GetServerMap.LinkData {
    type?: 'app';
  }

  export interface ServiceGroupLink {
    key: string;
    from: string;
    to: string;
    type: 'service';
    links: GetServerMap.LinkData[];
  }
}
