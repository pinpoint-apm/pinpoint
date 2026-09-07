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
    /**
     * map 탐색 조건(화면의 조회 조건 박스). 기준 application에서 몇 단계까지 뻗어 나갈지를
     * 정하는 값이라 기준 application이 있는 DEFAULT service에서만 싣는다. 그 외 service는
     * 조회 조건 박스 자체가 없고, 백엔드가 고정값으로 map을 그린다.
     */
    calleeRange?: number;
    callerRange?: number;
    wasOnly?: boolean;
    bidirectional?: boolean;
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
