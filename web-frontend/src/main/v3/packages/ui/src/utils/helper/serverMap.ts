import {
  ApplicationType,
  FilteredMapType as FilteredMap,
  GetServerMap,
  GetServiceMap,
} from '@pinpoint-fe/ui/src/constants';
import { Edge as ServerMapEdge, Node as ServerMapNode } from '@pinpoint-fe/server-map';

export type Edge = ServerMapEdge;
export type Node = ServerMapNode;

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

    return nodeList.length === 0 || nodeList.some(({ key }: { key: string }) => key === baseNodeId)
      ? baseNodeId
      : baseNodeId.replace(/(.*)\^(.*)/i, '$1^UNAUTHORIZED');
  }
  return '';
};

/**
 * Returns the base node key for a serviceMap response.
 * ServiceMap node keys use the format "serviceName^applicationName^serviceType",
 * so we find the node by matching applicationName and serviceType fields.
 */
export const getServiceMapBaseNodeId = ({
  application,
  applicationMapData,
}: {
  application: ApplicationType | null;
  applicationMapData?: GetServiceMap.ApplicationMapData;
}): string => {
  if (application && applicationMapData) {
    const nodeList = applicationMapData.nodeDataArray;

    const matchingNode = nodeList.find(
      (node): node is GetServiceMap.NodeData =>
        !GetServiceMap.isServiceGroupNode(node) &&
        node.applicationName === application.applicationName &&
        node.serviceType === application.serviceType,
    );
    if (matchingNode) return matchingNode.key;

    const unauthorizedNode = nodeList.find(
      (node): node is GetServiceMap.NodeData =>
        !GetServiceMap.isServiceGroupNode(node) &&
        node.applicationName === application.applicationName &&
        node.serviceType === 'UNAUTHORIZED',
    );
    if (unauthorizedNode) return unauthorizedNode.key;
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
