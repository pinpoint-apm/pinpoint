import { GetServerMap } from './GetServerMap';

// eslint-disable-next-line @typescript-eslint/no-namespace
export namespace GetServiceMap {
  export interface Parameters extends GetServerMap.Parameters {
    keepServiceNames?: string[];
  }

  export interface Response {
    applicationMapData: ApplicationMapData;
  }

  export interface ApplicationMapData {
    range: GetServerMap.Range;
    timestamp: number[];
    linkDataArray: GetServerMap.LinkData[];
    nodeDataArray: NodeViewEntry[];
  }

  /** Individual app node in serviceMap — key format: "serviceName^applicationName^serviceType" */
  export interface NodeData extends GetServerMap.NodeData {
    serviceName: string;
  }

  /** Collapsed service group node — key format: "serviceName" */
  export interface ServiceGroupNodeData {
    key: string;
    type: 'service';
    serviceName: string;
    nodes: NodeData[];
  }

  export type NodeViewEntry = NodeData | ServiceGroupNodeData;

  export function isServiceGroupNode(node: NodeViewEntry): node is ServiceGroupNodeData {
    return (node as ServiceGroupNodeData).type === 'service';
  }
}
