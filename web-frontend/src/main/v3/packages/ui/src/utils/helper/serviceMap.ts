import { GetServerMap, GetServiceMap } from '@pinpoint-fe/ui/src/constants';

// /serviceMap 응답을 GetServerMap.Response 형태로 변환.
// type:'service' 그룹은 그래프상 단일 노드로 그리되, 원본 자식 노드는 subNodes 필드에 보관해
// 그래프에서 노드를 좌클릭했을 때 팝업으로 자식 리스트를 펼칠 수 있게 한다.
export const flattenServiceMapResponse = (
  data: GetServiceMap.Response | undefined,
): GetServerMap.Response | undefined => {
  if (!data) return undefined;

  const emptyResponseStatistics: GetServerMap.ResponseStatistics = {
    Tot: 0,
    Sum: 0,
    Avg: 0,
    Max: 0,
  };
  const emptyHistogram: GetServerMap.Histogram = {
    '1s': 0,
    '3s': 0,
    '5s': 0,
    Slow: 0,
    Error: 0,
  };

  const nodeDataArray: GetServerMap.NodeData[] = [];
  for (const entry of data.applicationMapData.nodeDataArray) {
    if (entry.type === 'service') {
      const innerNodes = entry.nodes;
      const firstNode = innerNodes[0];
      nodeDataArray.push({
        key: entry.key,
        // group 노드는 백엔드가 key/serviceName/nodes 만 내려준다(ServiceGroupNodeView).
        // 나머지는 여기서 합성하므로, 소속 노드들이 공유하는 값은 firstNode 에서 가져온다.
        serviceKey: entry.key,
        serviceName: entry.serviceName,
        applicationName: entry.serviceName,
        serviceType: firstNode?.serviceType ?? 'UNKNOWN',
        serviceTypeCode: firstNode?.serviceTypeCode ?? 0,
        nodeCategory: firstNode?.nodeCategory ?? GetServerMap.NodeCategory.SERVER,
        isQueue: firstNode?.isQueue ?? false,
        isAuthorized: true,
        totalCount: innerNodes.reduce((acc, n) => acc + (n.totalCount ?? 0), 0),
        errorCount: innerNodes.reduce((acc, n) => acc + (n.errorCount ?? 0), 0),
        slowCount: innerNodes.reduce((acc, n) => acc + (n.slowCount ?? 0), 0),
        hasAlert: innerNodes.some((n) => n.hasAlert),
        responseStatistics: emptyResponseStatistics,
        histogram: emptyHistogram,
        apdex: {
          apdexScore: 0,
          apdexFormula: { satisfiedCount: 0, toleratingCount: 0, totalSamples: 0 },
        },
        timeSeriesHistogram: [],
        instanceCount: innerNodes.length,
        instanceErrorCount: 0,
        agents: [],
        subNodes: innerNodes,
      });
    } else {
      nodeDataArray.push(entry);
    }
  }

  const linkDataArray: GetServerMap.LinkData[] = [];
  for (const entry of data.applicationMapData.linkDataArray) {
    if (entry.type === 'service') {
      const innerLinks = entry.links;
      const firstLink = innerLinks[0];
      linkDataArray.push({
        key: entry.key,
        from: entry.from,
        to: entry.to,
        sourceInfo: firstLink?.sourceInfo,
        targetInfo: firstLink?.targetInfo,
        filter: firstLink?.filter,
        responseStatistics: emptyResponseStatistics,
        histogram: emptyHistogram,
        timeSeriesHistogram: [],
        totalCount: innerLinks.reduce((acc, l) => acc + (l.totalCount ?? 0), 0),
        errorCount: innerLinks.reduce((acc, l) => acc + (l.errorCount ?? 0), 0),
        slowCount: innerLinks.reduce((acc, l) => acc + (l.slowCount ?? 0), 0),
        hasAlert: innerLinks.some((l) => l.hasAlert),
        subLinks: innerLinks,
      } as GetServerMap.LinkData);
    } else {
      linkDataArray.push(entry);
    }
  }

  return {
    applicationMapData: {
      ...data.applicationMapData,
      nodeDataArray,
      linkDataArray,
    },
  };
};

// 고른 대상은 servermap/servicemap/filteredMap 노드·링크 중 무엇이든 될 수 있다. 타입 유니온을
// 늘리는 대신 판별에 쓰는 두 필드만 구조로 본다.
type ServiceGroupCandidate = { subNodes?: unknown[]; subLinks?: unknown[] };

/**
 * 고른 대상이 service group(접힌 service)인지.
 *
 * 판별은 `subNodes`/`subLinks`로 한다 — `flattenServiceMapResponse`가 `type:'service'` 응답에만
 * 담아 두는 필드라, 이 필드의 유무가 곧 "접힌 service인가"다. servermap/filteredMap 응답에는
 * 없으므로 그 화면들에서는 항상 false가 되어 동작이 달라지지 않는다.
 *
 * group 노드는 **기준 application이 없다.** 그런데 `flattenServiceMapResponse`가 화면에 이름을
 * 그리려고 `applicationName`에 serviceName을 넣어 두기 때문에, 그대로 조회에 쓰면 service를
 * application인 것처럼 묻게 된다(그 이름의 application은 없으니 데이터가 비어서 온다).
 * 그래서 **조회하는 컴포넌트는 group 대상을 조회 대상으로 삼지 않는다.**
 */
export const isServiceGroupNode = (node?: unknown): boolean =>
  ((node as ServiceGroupCandidate)?.subNodes?.length ?? 0) > 0;

/** 고른 대상이 service group 링크인지. 판별 근거는 {@link isServiceGroupNode}와 같다. */
export const isServiceGroupLink = (link?: unknown): boolean =>
  ((link as ServiceGroupCandidate)?.subLinks?.length ?? 0) > 0;

/** 고른 대상(노드 또는 링크)이 service group인지. */
export const isServiceGroupTarget = (data?: unknown): boolean =>
  isServiceGroupNode(data) || isServiceGroupLink(data);

/**
 * 주어진 key의 노드가 service group(접힌 service)인지 찾는다. 아니면 undefined다.
 * 판별 근거는 {@link isServiceGroupNode} 참고.
 *
 * 두 곳에서 쓴다.
 * 1. 좌클릭 — 자식 노드 목록 팝업을 연다.
 * 2. 우클릭 — 필터 메뉴를 **열지 않는다**. group 노드의 id는 serviceName 하나뿐이라 기준
 *    application이 없고, filteredMap은 기준 application 없이 조회가 성립하지 않는다
 *    (`getFilterTargetApplication`이 null). 메뉴를 띄우면 눌러도 아무 일이 일어나지 않는
 *    항목이 된다. 필터를 걸 수 없는 노드에 메뉴를 띄우지 않는 기존 처리와 같은 방식이다.
 */
export const findServiceGroupNode = <T extends GetServerMap.NodeData>(
  nodes: T[] | undefined,
  key?: string,
): T | undefined => nodes?.find((node) => node.key === key && isServiceGroupNode(node));

/**
 * 주어진 key의 링크가 service group 링크인지 찾는다. 아니면 undefined다.
 *
 * 백엔드는 **양쪽 끝이 모두 펼쳐진 service일 때만** 평범한 링크로 내려준다
 * (`ServiceMapViewBuilder#buildLinks`의 `fromExpanded && toExpanded`). 한쪽이라도 접혀 있으면
 * `type:'service'`로 묶여 내려오고, `flattenServiceMapResponse`가 `subLinks`에 담는다.
 *
 * 즉 **`subLinks`가 있으면 Application→Application이 아니다** — Application→Service,
 * Service→Application, Service→Service 세 경우가 모두 여기에 해당한다. 이 링크들은 한쪽 끝에
 * application이 없어 필터가 반쪽만 걸리므로 filteredMap으로 연결하지 않는다. (노드와 같은 이유)
 */
export const findServiceGroupLink = <T extends GetServerMap.LinkData>(
  links: T[] | undefined,
  key?: string,
): T | undefined => links?.find((link) => link.key === key && isServiceGroupLink(link));
