import { atom } from 'jotai';
import {
  EXPERIMENTAL_CONFIG_KEYS,
  FilteredMapType as FilteredMap,
  GetHistogramStatistics,
  GetServerMap,
  AgentOverview,
} from '@pinpoint-fe/ui/src/constants';
import { Node, Edge } from '@pinpoint-fe/server-map';
import { isNodeOfApplication } from '@pinpoint-fe/ui/src/utils/helper/serverMap';

export type CurrentTarget = {
  id?: string;
  type?: 'node' | 'edge';
  applicationName?: string;
  imgPath?: string;
  serviceType?: string;
  nodes?: Node[];
  source?: string;
  target?: string;
  edges?: Edge[];
  hint?: {
    [key: string]: FilteredMap.FilterTargetRpcList;
  };
};

type ServerMapNodeDataArray = (GetServerMap.NodeData | FilteredMap.NodeData)[];
type ServerMapNodeLinkArray = (GetServerMap.LinkData | FilteredMap.LinkData)[];

export const serverMapDataAtom = atom<GetServerMap.Response | FilteredMap.Response | undefined>(
  undefined,
);

// 서버맵에서 선택된 타겟
export const serverMapCurrentTargetAtom = atom<CurrentTarget | undefined>(undefined);

export const serverMapCurrentTargetDataAtom = atom((get) => {
  const currentTarget = get(serverMapCurrentTargetAtom);
  const serverMapData = get(serverMapDataAtom);

  if (currentTarget?.serviceType === 'USER') {
    return (serverMapData?.applicationMapData?.nodeDataArray as ServerMapNodeDataArray)?.find(
      ({ serviceType }) => serviceType === currentTarget.serviceType,
    );
  } else if (currentTarget?.type === 'node') {
    const fallbackKey = `${currentTarget?.applicationName}^${currentTarget?.serviceType}`;
    const allNodes =
      (serverMapData?.applicationMapData?.nodeDataArray as ServerMapNodeDataArray) || [];
    // service group 노드는 그래프상 단일 노드로 그리지만, 팝업에서 자식(subNodes)을 클릭하면
    // currentTarget이 자식 노드 key를 가리키므로 lookup 대상에 자식까지 포함한다.
    const flattenedNodes = allNodes.flatMap((node) => {
      const subNodes = (node as GetServerMap.NodeData).subNodes;
      return Array.isArray(subNodes) ? [node, ...subNodes] : [node];
    });
    return flattenedNodes.find((node) => {
      const nodeKey = (node as GetServerMap.NodeData).nodeKey;
      return (
        node.key === currentTarget?.id ||
        node.key === fallbackKey ||
        nodeKey === currentTarget?.id ||
        nodeKey === fallbackKey ||
        // 기준 노드(base node)로 세운 target은 id가 없어 이름으로만 찾을 수 있다. key 형식이
        // map API마다 다르므로(servermap 2단, servicemap 3단) 형식에 무관하게 비교한다.
        // `nodeKey`(2단)의 serviceType은 `getName()` 형식이라 여기서 만든 desc 형식과
        // 어긋나는 타입이 있어(UNKNOWN_DB_EXECUTE_QUERY→UNKNOWN_DB) 그것만으로는 부족하다.
        isNodeOfApplication(node as GetServerMap.NodeData, currentTarget)
      );
    });
  } else if (currentTarget?.type === 'edge') {
    const allLinks =
      (serverMapData?.applicationMapData?.linkDataArray as ServerMapNodeLinkArray) || [];
    // service group 링크는 그래프상 단일 엣지로 그리지만, 팝업에서 자식(subLinks)을 클릭하면
    // currentTarget이 자식 링크 key를 가리키므로 lookup 대상에 자식까지 포함한다.
    const flattenedLinks = allLinks.flatMap((link) => {
      const subLinks = (link as GetServerMap.LinkData).subLinks;
      return Array.isArray(subLinks) ? [link, ...subLinks] : [link];
    });
    return flattenedLinks.find(
      (link) =>
        link.key === currentTarget?.id ||
        (link as GetServerMap.LinkData).linkKey === currentTarget?.id,
    );
  } else {
    return undefined;
  }
});

export const currentNodeStatisticsAtom = atom<GetHistogramStatistics.Response | undefined>(
  undefined,
);

// server-list 선택시
export const currentServerAtom = atom<AgentOverview.Instance | undefined>(undefined);

export const currentServerAgentIdAtom = atom<string | undefined>((get) => {
  const currentServer = get(currentServerAtom);
  return currentServer?.agentId;
});

export const realtimeDateRanage = atom<{ from: Date; to: Date } | undefined>(undefined);

export const serverMapChartTypeAtom = atom<'scatter' | 'heatmap'>('heatmap');
