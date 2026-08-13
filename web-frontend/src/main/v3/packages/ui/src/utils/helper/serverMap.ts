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

/**
 * map에서 클릭한 노드/링크의 id에서 application을 읽는다. 읽을 수 없으면 null이다.
 *
 * id의 형태가 API마다 다르다(백엔드 `NodeName` / `ServiceNodeName`).
 * - servermap: `applicationName^serviceType` (2-part)
 * - servicemap: `serviceName^applicationName^serviceType` (3-part)
 *
 * 뒤 두 토큰이 항상 application이라 `parseBaseNodeId`와 같은 규칙으로 읽는다. serviceName은
 * escape되지 않으므로 앞쪽 토큰은 더 늘어날 수 있지만, applicationName은 백엔드가 '^'를
 * escape하므로(`ApplicationNameEscaper`) 뒤에서 세는 것은 안전하다.
 * 마지막 토큰(serviceType)은 두 API 모두 `ServiceType.toString()`이라 값이 같다.
 *
 * servicemap의 service group(접힌 service) 노드·링크는 id가 serviceName 하나뿐이라 기준
 * application이 없다. URL 세그먼트를 파싱하는 `getApplicationTypeAndName`을 쓰면 3-part id에서
 * applicationName에 `serviceName^applicationName`이 들어오므로 이 함수를 쓴다.
 */
export const parseNodeApplication = (nodeId = ''): ApplicationType | null => {
  const tokens = nodeId.split('^');

  if (tokens.length < 2) {
    return null;
  }

  const applicationName = tokens[tokens.length - 2];
  const serviceType = tokens[tokens.length - 1];

  if (!applicationName || !serviceType) {
    return null;
  }

  return { applicationName, serviceType };
};

const isSameApplication = (a?: ApplicationType | null, b?: ApplicationType | null) =>
  !!a && !!b && a.applicationName === b.applicationName && a.serviceType === b.serviceType;

/**
 * map 노드가 이 application의 것인지.
 *
 * **key의 뒤 두 토큰으로 비교한다.** 2단/3단 형식 차이에 걸리지 않는 유일한 방법이다.
 * 2단짜리 `nodeKey`와 비교하는 방법도 있지만, 그 필드의 serviceType은 `ServiceType.getName()`
 * 형식인데 URL에서 온 값은 `getDesc()` 형식이라 둘이 다른 타입에서 어긋난다
 * (`UNKNOWN_DB_EXECUTE_QUERY`의 desc는 `UNKNOWN_DB`, `SPRING_ORM_IBATIS`는 `SPRING`).
 * `key`는 두 형식 모두 desc이므로 같은 기준끼리 비교하게 된다.
 *
 * 권한이 없는 노드는 백엔드가 serviceType을 UNAUTHORIZED로 치환해 내려주므로 이름만 비교한다.
 */
export const isNodeOfApplication = (
  node: GetServerMap.NodeData | FilteredMap.NodeData,
  application?: ApplicationType | null,
) => {
  if (!application?.applicationName) {
    return false;
  }

  return (
    isSameApplication(parseNodeApplication(node.key), application) ||
    (node.applicationName === application.applicationName && node.serviceType === 'UNAUTHORIZED')
  );
};

/** map에서 이 application에 해당하는 노드. 없으면 undefined다. */
export const findNodeOfApplication = <T extends GetServerMap.NodeData | FilteredMap.NodeData>(
  nodes: T[] | undefined,
  application?: ApplicationType | null,
): T | undefined => nodes?.find((node) => isNodeOfApplication(node, application));

/**
 * map 링크가 이 두 application을 잇는 것인지. 노드와 같은 이유로 `key`의 뒤 두 토큰으로 비교한다.
 * 링크 key는 노드 이름 둘을 '~'로 이은 것이다(`{from}~{to}`).
 */
export const isLinkOfApplications = (
  link: GetServerMap.LinkData | FilteredMap.LinkData,
  from?: ApplicationType | null,
  to?: ApplicationType | null,
) => {
  const [fromSegment, toSegment] = (link.key ?? '').split('~');

  return (
    isSameApplication(parseNodeApplication(fromSegment), from) &&
    isSameApplication(parseNodeApplication(toSegment), to)
  );
};

/** map에서 이 두 application을 잇는 링크. 없으면 undefined다. */
export const findLinkOfApplications = <T extends GetServerMap.LinkData | FilteredMap.LinkData>(
  links: T[] | undefined,
  from?: ApplicationType | null,
  to?: ApplicationType | null,
): T | undefined => links?.find((link) => isLinkOfApplications(link, from, to));

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
    // 권한이 없는 노드는 백엔드가 serviceType을 UNAUTHORIZED로 치환해 내려준다.
    // serviceMap 응답에서는 key가 3-part(serviceName^app^UNAUTHORIZED)이고 nodeKey가
    // 2-part(app^UNAUTHORIZED)이므로, 합성한 2-part id 대신 실제 노드의 key를 반환해
    // cytoscape id(=node.key)와 일치시켜야 센터링/하이라이트가 동작한다.
    const unauthorizedKey = baseNodeId.replace(/\^[^^]*$/, '^UNAUTHORIZED');
    const unauthorizedNode = (nodeList as GetServerMap.NodeData[]).find(
      (n) => n.key === unauthorizedKey || n.nodeKey === unauthorizedKey,
    );
    if (unauthorizedNode) return unauthorizedNode.key;
    // 권한 없는 노드가 service group의 자식인 경우, 그래프에 그려지는 것은 그룹 노드이므로
    // subNodes까지 탐색해 그룹 노드의 key를 base로 사용한다. (위 groupContaining과 동일한 처리)
    const groupContainingUnauthorized = (nodeList as GetServerMap.NodeData[]).find(
      (n) =>
        Array.isArray(n.subNodes) &&
        n.subNodes.some(
          (inner) => inner.key === unauthorizedKey || inner.nodeKey === unauthorizedKey,
        ),
    );
    if (groupContainingUnauthorized) return groupContainingUnauthorized.key;
    return unauthorizedKey;
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
