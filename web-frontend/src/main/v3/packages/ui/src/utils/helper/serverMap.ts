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

export const getTimeSeriesApdexInfo = (
  node: GetServerMap.NodeData | FilteredMap.NodeData,
  timestamp: number[] = [],
) => {
  const { isAuthorized, timeSeriesHistogram } = node;

  if (!isAuthorized || !timeSeriesHistogram) {
    return [];
  }

  const maxSlots = 24; // 전체 원을 최대 24개의 slot으로 나눈다.
  const dataLength = timestamp?.length;

  const oneSecond =
    timeSeriesHistogram?.find((time) => time.key === '1s' || time.key === '100ms')?.values || [];
  const threeSecond =
    timeSeriesHistogram?.find((time) => time.key === '3s' || time.key === '300ms')?.values || [];
  const total = timeSeriesHistogram?.find((time) => time.key === 'Tot')?.values || [];

  if (
    oneSecond.length !== dataLength ||
    threeSecond.length !== dataLength ||
    total.length !== dataLength
  ) {
    return [];
  }

  if (dataLength <= maxSlots) {
    return timestamp?.map((time, index) => {
      if (total?.[index] === 0) {
        return 1;
      }

      return (oneSecond?.[index] * 1 + threeSecond?.[index] * 0.5) / total?.[index];
    });
  }

  const groupSize = Math.floor(dataLength / maxSlots); // 각 slot에 최소 몇 개의 데이터씩 들어가는지
  let remainder = dataLength % maxSlots; // 나머지는 앞쪽 slot에 하나씩 더 넣기

  let i = 0;
  const result = [];

  while (i < dataLength) {
    const currentSlotSize = groupSize + (remainder > 0 ? 1 : 0);
    remainder = Math.max(0, remainder - 1); // 나머지 하나 줄이기

    const mergedTotal = total?.slice(i, i + currentSlotSize)?.reduce((t, acc) => {
      return acc + t;
    }, 0);

    if (mergedTotal === 0) {
      result.push(1);
    } else {
      const mergedOneSecond = oneSecond?.slice(i, i + currentSlotSize)?.reduce((t, acc) => {
        return acc + t;
      }, 0);
      const mergedThreeSecond = threeSecond?.slice(i, i + currentSlotSize)?.reduce((t, acc) => {
        return acc + t;
      }, 0);

      result.push((mergedOneSecond * 1 + mergedThreeSecond * 0.5) / mergedTotal);
    }
    i += currentSlotSize;
  }

  return result;
};
