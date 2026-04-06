import {
  ApplicationType,
  FilteredMapType as FilteredMap,
  GetServerMap,
} from '@pinpoint-fe/ui/src/constants';
import { Edge as ServerMapEdge, Node as ServerMapNode } from '@pinpoint-fe/server-map';

export type Edge = ServerMapEdge;
export type Node = ServerMapNode;

/**
 * Unescapes application name from backend format.
 * Backend escapes '^' as '\^' and '\' as '\\'
 * e.g. "my\^app" → "my^app"
 */
export const unescapeApplicationName = (escaped: string): string => {
  let result = '';
  let i = 0;
  while (i < escaped.length) {
    if (escaped[i] === '\\' && i + 1 < escaped.length) {
      result += escaped[i + 1];
      i += 2;
    } else {
      result += escaped[i];
      i++;
    }
  }
  return result;
};

const findLastUnescapedCaret = (str: string): number => {
  for (let i = str.length - 1; i >= 0; i--) {
    if (str[i] === '^') {
      let backslashes = 0;
      let j = i - 1;
      while (j >= 0 && str[j] === '\\') {
        backslashes++;
        j--;
      }
      if (backslashes % 2 === 0) return i;
    }
  }
  return -1;
};

const findFirstUnescapedCaret = (str: string): number => {
  for (let i = 0; i < str.length; i++) {
    if (str[i] === '^') {
      let backslashes = 0;
      let j = i - 1;
      while (j >= 0 && str[j] === '\\') {
        backslashes++;
        j--;
      }
      if (backslashes % 2 === 0) return i;
    }
  }
  return -1;
};

/**
 * Parses a node key into its components.
 * - enableServiceMap=false: "applicationName^serviceType"
 * - enableServiceMap=true:  "serviceName^applicationName(escaped)^serviceType"
 *   where applicationName may contain escaped carets (\^) that display as ^
 */
export const parseNodeKey = (
  key: string,
): { serviceName?: string; applicationName: string; serviceType: string } => {
  const lastCaretPos = findLastUnescapedCaret(key);

  if (lastCaretPos === -1) {
    return { applicationName: key, serviceType: '' };
  }

  const serviceType = key.substring(lastCaretPos + 1);
  const rest = key.substring(0, lastCaretPos);
  const firstCaretPos = findFirstUnescapedCaret(rest);

  if (firstCaretPos === -1) {
    // 2-part format: rest is applicationName (no escaping in this format)
    return { applicationName: rest, serviceType };
  }

  // 3-part format: serviceName^escapedApplicationName^serviceType
  const serviceName = rest.substring(0, firstCaretPos);
  const escapedApplicationName = rest.substring(firstCaretPos + 1);
  return {
    serviceName,
    applicationName: unescapeApplicationName(escapedApplicationName),
    serviceType,
  };
};

export const getBaseNodeId = ({
  application,
  applicationMapData,
  enableServiceMap = false,
}: {
  application: ApplicationType | null;
  applicationMapData?: GetServerMap.ApplicationMapData | FilteredMap.ApplicationMapData;
  enableServiceMap?: boolean;
}) => {
  if (application && applicationMapData) {
    const nodeList = applicationMapData.nodeDataArray;
    const { applicationName, serviceType } = application;

    if (enableServiceMap) {
      if (nodeList.length === 0) {
        // 아직 데이터가 없는 경우 빈 문자열 반환.
        // 2-part 폴백(applicationName^serviceType)을 반환하면 이후 실제 노드가 로드될 때
        // 3-part 키(serviceName^applicationName^serviceType)로 바뀌면서 그래프가 재배치됨.
        return '';
      }
      // NodeData.applicationName is already unescaped, use it directly for matching
      const matchingNode = nodeList.find(
        (node: { applicationName: string; serviceType: string }) =>
          node.applicationName === applicationName && node.serviceType === serviceType,
      );
      if (matchingNode) {
        return (matchingNode as { key: string }).key;
      }
      // Not found — look for UNAUTHORIZED node with same applicationName
      const unauthorizedNode = nodeList.find(
        (node: { applicationName: string; serviceType: string }) =>
          node.applicationName === applicationName && node.serviceType === 'UNAUTHORIZED',
      );
      if (unauthorizedNode) {
        return (unauthorizedNode as { key: string }).key;
      }
      return `${applicationName}^UNAUTHORIZED`;
    }

    const baseNodeId = `${applicationName}^${serviceType}`;
    return nodeList.length === 0 || nodeList.some(({ key }: { key: string }) => key === baseNodeId)
      ? baseNodeId
      : baseNodeId.replace(/(.*)\^(.*)/i, '$1^UNAUTHORIZED');
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
