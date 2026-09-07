import {
  ApplicationType,
  FilteredMapType as FilteredMap,
  GetServerMap,
} from '@pinpoint-fe/ui/src/constants';
import { Edge as ServerMapEdge, Node as ServerMapNode } from '@pinpoint-fe/server-map';

export type Edge = ServerMapEdge;
export type Node = ServerMapNode;

/**
 * 기준 노드 id에서 application을 읽는다. 읽을 수 없으면 빈 문자열이다.
 * (`getBaseNodeId`가 실제 node.key를 돌려주므로 그 형식들을 그대로 받는다.)
 *
 * 형식 판별은 `parseNodeApplication` 하나에 맡긴다 — 규칙이 바뀔 때 고칠 곳이 하나여야 한다.
 */
export const parseBaseNodeId = (
  baseNodeId: string,
): { applicationName: string; serviceType: string } => {
  const parsed = parseNodeApplication(baseNodeId);

  return {
    applicationName: parsed?.applicationName ?? '',
    serviceType: parsed?.serviceType ?? '',
  };
};

/**
 * 앞에서부터 첫 번째 **escape되지 않은** '^'의 위치. 없으면 -1.
 * 앞에 붙은 '\'의 개수가 짝수면 escape되지 않은 것이다(백엔드 `ServiceNodeNameParser`와 같은 규칙).
 */
const findServiceDelimiter = (value: string) => {
  for (let i = 0; i < value.length; i++) {
    if (value[i] !== '^') {
      continue;
    }

    let backslashCount = 0;
    for (let j = i - 1; j >= 0 && value[j] === '\\'; j--) {
      backslashCount++;
    }

    if (backslashCount % 2 === 0) {
      return i;
    }
  }

  return -1;
};

/** 백엔드 `ApplicationNameEscaper.unescape`와 같다. '\X'를 'X'로 되돌린다. */
const unescapeApplicationName = (value: string) => value.replace(/\\(.)/g, '$1');

/**
 * map에서 클릭한 노드/링크의 id에서 application을 읽는다. 읽을 수 없으면 null이다.
 *
 * id의 형태가 API마다 다르다(백엔드 `NodeName` / `ServiceNodeName`).
 * - servermap: `applicationName^serviceType` (2단, applicationName을 escape하지 않는다)
 * - servicemap: `serviceName^escape(applicationName)^serviceType` (3단)
 *
 * **백엔드 `ServiceNodeNameParser`와 같은 규칙으로 읽는다.** serviceType은 마지막 '^' 뒤,
 * applicationName은 그 앞부분을 첫 번째 escape되지 않은 '^'에서 잘라낸 나머지다.
 * 단순히 '^'로 쪼개 뒤 두 토큰을 쓰면, applicationName 안의 escape된 '^'(`a\^b`)를 구분자로
 * 읽어 이름이 잘린다(`svc^a\^b^TOMCAT` → `b`).
 *
 * 3단일 때만 unescape한다. 2단은 백엔드가 escape하지 않으므로(`NodeName.newNodeKey`)
 * unescape하면 이름에 든 '\'가 사라진다.
 *
 * 마지막 토큰(serviceType)은 두 API 모두 `ServiceType.toString()`이라 값이 같다.
 *
 * servicemap의 service group(접힌 service) 노드·링크는 id가 serviceName 하나뿐이라 기준
 * application이 없다. URL 세그먼트를 파싱하는 `getApplicationTypeAndName`을 쓰면 3단 id에서
 * applicationName에 `serviceName^applicationName`이 들어오므로 이 함수를 쓴다.
 */
export const parseNodeApplication = (nodeId = ''): ApplicationType | null => {
  const lastDelimiter = nodeId.lastIndexOf('^');

  if (lastDelimiter === -1) {
    return null;
  }

  const serviceType = nodeId.slice(lastDelimiter + 1);
  const serviceNameAndApplication = nodeId.slice(0, lastDelimiter);
  const serviceDelimiter = findServiceDelimiter(serviceNameAndApplication);
  const applicationName =
    serviceDelimiter === -1
      ? serviceNameAndApplication
      : unescapeApplicationName(serviceNameAndApplication.slice(serviceDelimiter + 1));

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

/**
 * map에서 고른 대상이 **merged**인지 — 그래프가 여러 노드/링크를 하나로 묶어 그린 것.
 *
 * 묶인 것의 id는 합성값(`{source}_MergeSingleNodesByServerMap^MONGO`)이라 map 데이터에서 찾을 수
 * 없다. 그래서 "묶음 목록(`nodes`/`edges`)은 있는데 그 id로 찾은 데이터는 없다"로 판별한다.
 *
 * 목록에서 자식 하나를 고르면 `nodes`는 목록을 계속 보여주려고 그대로 남고 id만 실제 노드 key로
 * 바뀐다. 그러면 데이터가 잡히므로 더 이상 merged가 아니다.
 */
export const isMergedMapTarget = (
  currentTarget: { nodes?: unknown[]; edges?: unknown[] } | undefined,
  currentTargetData: unknown,
): boolean => !!(currentTarget?.nodes || currentTarget?.edges) && !currentTargetData;

/**
 * 이 노드를 호출하는 application(들어오는 링크의 출발지). 하나로 정해지지 않으면 undefined다.
 *
 * 통계 API는 `applicationName`/`serviceTypeName`을 **상대편(기준) application**으로 쓰고
 * `nodeKey`로 볼 노드를 정한다(`ServerMapHistogramController#getStatisticsFromServerMap`).
 * DB·캐시처럼 자기 agent가 없는 리프 노드는 호출자 링크에서 수치가 나오므로, 기준을 노드 자신으로
 * 두면 백엔드가 그 노드를 map 중심으로 놓는 다른 분기로 빠진다.
 *
 * 들어오는 링크가 여럿이면 어느 쪽을 기준으로 삼을지 정할 수 없으므로 undefined를 돌려주고,
 * 호출부가 기존 기준(노드 자신)을 그대로 쓰게 한다.
 */
export const findCallerApplication = (
  links: (GetServerMap.LinkData | FilteredMap.LinkData)[] | undefined,
  nodeKey: string | undefined,
): ApplicationType | undefined => {
  if (!nodeKey) {
    return undefined;
  }

  const inboundLinks = (links ?? []).filter((link) => link.to === nodeKey);
  if (inboundLinks.length !== 1) {
    return undefined;
  }

  const { applicationName, serviceType } = inboundLinks[0].sourceInfo ?? {};
  return applicationName && serviceType ? { applicationName, serviceType } : undefined;
};

/**
 * 우측 패널의 조회가 기준으로 삼을 application. 경로에 application이 없는 화면
 * (service 전체를 모아 그린 servicemap)에서 이 값이 기준이 된다.
 *
 * - merged 대상이면 **없다.** 여러 application을 묶은 것이라 기준이 성립하지 않는다.
 *   `applicationName` 자리에 있는 것은 그래프가 붙인 라벨("total: 2")이므로 그대로 쓰면 그 이름으로
 *   조회가 나간다. servermap은 경로의 application이 기준이 되어 가려져 있었고, 경로에 application이
 *   없는 servicemap에서 드러났다. (이슈 #10587)
 * - **merged 묶음 목록에서 고른 노드는 그 노드의 호출자가 기준이다.** servermap에서는 경로의
 *   application(map 중심)이 그 자리를 맡는데, 경로에 application이 없는 servicemap에서 노드 자신을
 *   기준으로 두면 요청이 다른 분기로 빠져 수치가 달라진다(→ `findCallerApplication`).
 * - 그 외 노드면 그 노드. 클릭 즉시 채워지는 `currentTarget`에서 읽는다(조회된 데이터를 기다리지
 *   않는다). 노드 자신을 기준으로 삼는 것은 그 application을 직접 열어 본 것과 같은 결과가 된다.
 * - 링크면 출발지 노드. 링크 통계의 기준도 출발지이므로 같은 기준이다.
 */
export const getSelectedTargetApplication = (
  currentTarget:
    | {
        type?: 'node' | 'edge';
        applicationName?: string;
        serviceType?: string;
        nodes?: unknown[];
        edges?: unknown[];
      }
    | undefined,
  // map API마다 노드/링크 타입이 갈리므로(servermap · servicemap · filteredMap)
  // 여기서는 읽는 필드만 좁혀서 본다.
  currentTargetData: unknown,
  /** merged 묶음에서 고른 노드의 호출자를 찾는 데 쓴다. 없으면 그 보정을 하지 않는다. */
  links?: (GetServerMap.LinkData | FilteredMap.LinkData)[],
): ApplicationType | undefined => {
  if (isMergedMapTarget(currentTarget, currentTargetData)) {
    return undefined;
  }

  if (currentTarget?.type === 'node') {
    const isFromMergedGroup = !!(currentTarget.nodes || currentTarget.edges);
    if (isFromMergedGroup) {
      const caller = findCallerApplication(
        links,
        (currentTargetData as GetServerMap.NodeData | undefined)?.key,
      );
      if (caller) {
        return caller;
      }
    }

    const { applicationName, serviceType } = currentTarget;
    return applicationName && serviceType ? { applicationName, serviceType } : undefined;
  }

  const sourceInfo = (currentTargetData as GetServerMap.LinkData | undefined)?.sourceInfo;
  return sourceInfo?.applicationName && sourceInfo?.serviceType
    ? { applicationName: sourceInfo.applicationName, serviceType: sourceInfo.serviceType }
    : undefined;
};
