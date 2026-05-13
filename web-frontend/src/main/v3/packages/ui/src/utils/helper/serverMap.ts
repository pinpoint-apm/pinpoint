import {
  ApplicationType,
  FilteredMapType as FilteredMap,
  GetServerMap,
} from '@pinpoint-fe/ui/src/constants';
import { Edge as ServerMapEdge, Node as ServerMapNode } from '@pinpoint-fe/server-map';

export type Edge = ServerMapEdge;
export type Node = ServerMapNode;

// node/link key는 2-part(applicationName^serviceType) 또는 3-part(serviceName^applicationName^serviceType)로 들어올 수 있다.
// 마지막 두 토큰이 항상 [applicationName, serviceType]이므로 뒤에서 잘라낸다.
export const parseBaseNodeId = (
  baseNodeId: string,
): { applicationName: string; serviceType: string } => {
  const tokens = baseNodeId.split('^');
  return {
    applicationName: tokens[tokens.length - 2] ?? '',
    serviceType: tokens[tokens.length - 1] ?? '',
  };
};

// applicationName^serviceType (2-part). serviceMap 응답 노드는 key가
// serviceName^applicationName^serviceType (3-part)지만 별도로 nodeKey(2-part) 필드를 가진다.
// 매칭 시 둘 다 비교하고, 매칭된 노드의 실제 key를 반환하여 cytoscape id와 일치시킨다.
export const getBaseNodeId = ({
  application,
  applicationMapData,
}: {
  application: ApplicationType | null;
  applicationMapData?: GetServerMap.ApplicationMapData | FilteredMap.ApplicationMapData;
}) => {
  if (application && applicationMapData) {
    const nodeList = applicationMapData.nodeDataArray;
    const baseNodeId = `${application?.applicationName}^${application?.serviceType}`;

    if (nodeList.length === 0) return baseNodeId;

    const matched = (nodeList as GetServerMap.NodeData[]).find(
      (n) => n.key === baseNodeId || n.nodeKey === baseNodeId,
    );
    if (matched) return matched.key;
    // 사용자가 진입한 application이 service group의 자식 노드인 경우, 그래프에 그려지는
    // service group 노드의 key를 base로 사용한다.
    const groupContaining = (nodeList as GetServerMap.NodeData[]).find(
      (n) =>
        Array.isArray(n.subNodes) &&
        n.subNodes.some((inner) => inner.key === baseNodeId || inner.nodeKey === baseNodeId),
    );
    if (groupContaining) return groupContaining.key;
    return baseNodeId.replace(/\^[^^]*$/, '^UNAUTHORIZED');
  }
  return '';
};

// apdexSlot은 백엔드가 미리 집계해 보내는 0~24개의 Apdex 점수 배열이다.
// 배열 길이에 맞춰 칸을 나누어 timeSeriesApdexInfo로 렌더링한다.
const APDEX_SLOT_MAX = 24;
// 백엔드의 ApdexScoreSlotViewBuilder.UNCOLLECTED_VALUE. 데이터가 수집되지 않은 슬롯을 의미하며
// 시각화에서는 Excellent(1)와 동일하게 다룬다.
const APDEX_UNCOLLECTED = -1;

export const getTimeSeriesApdexInfo = (
  node: GetServerMap.NodeData | FilteredMap.NodeData,
): number[] => {
  const { isAuthorized } = node;
  const apdexSlot = 'apdexSlot' in node ? node.apdexSlot : undefined;

  if (!isAuthorized || !apdexSlot || apdexSlot.length === 0) {
    return [];
  }

  return apdexSlot
    .slice(0, APDEX_SLOT_MAX)
    .map((score) => (score === APDEX_UNCOLLECTED ? 1 : score));
};
