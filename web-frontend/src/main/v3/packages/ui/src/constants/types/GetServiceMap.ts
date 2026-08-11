import { GetServerMap } from './GetServerMap';

export namespace GetServiceMap {
  export interface Parameters {
    /**
     * DEFAULT service에서만 필수다. 그 외 service는 백엔드가 service에 소속된 모든
     * application을 source로 사용하므로 값을 싣지 않는다.
     */
    applicationName?: string;
    serviceTypeName?: string;
    serviceTypeCode?: number;
    from: number | string;
    to: number | string;
    useStatisticsAgentState?: boolean;
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
