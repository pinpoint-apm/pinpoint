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

/**
 * service group(접힌 service) 노드인지 찾는다. 아니면 undefined다.
 *
 * 판별은 `subNodes`로 한다 — `flattenServiceMapResponse`가 `type:'service'` 응답에만 담아 두는
 * 필드라, 이 필드의 유무가 곧 "접힌 service인가"다. servermap/filteredMap 응답에는 없으므로
 * 그 화면들에서는 항상 undefined가 되어 동작이 달라지지 않는다.
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
): T | undefined =>
  nodes?.find(
    (node) => node.key === key && Array.isArray(node.subNodes) && node.subNodes.length > 0,
  );

/**
 * service group 링크인지 찾는다. 아니면 undefined다.
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
): T | undefined =>
  links?.find(
    (link) => link.key === key && Array.isArray(link.subLinks) && link.subLinks.length > 0,
  );

/**
 * map 검색 목록의 한 항목.
 *
 * service group(접힌 service) 노드는 그래프에 단일 노드로 그려지고 `applicationName`이
 * serviceName이라, 목록에 노드 배열을 그대로 넘기면 그 service에 묶인 application(b-1, b-2)은
 * 이름으로 찾을 수 없다. 그래서 group은 자기 자신과 소속 application을 모두 항목으로 편다.
 */
export interface ServerMapSearchItem {
  node: GetServerMap.NodeData;
  /**
   * 이 항목이 service group 노드 자체일 때 true.
   *
   * group은 application 묶음이라 serviceType이 없다. `flattenServiceMapResponse`가 자식 첫
   * 노드의 타입을 합성해 채워 두지만(merge 판정에 쓰인다) 사용자에게 보일 값은 아니다 —
   * 이름 뒤에 붙이면 B가 TOMCAT application인 것처럼 읽힌다.
   */
  isServiceGroup?: boolean;
  /**
   * 이 항목이 service group에 묶인 자식 application일 때 그 group 노드. 아니면 undefined다.
   *
   * 자식 application은 그래프에 노드가 없으므로(그려진 것은 group 하나뿐이다) 목록에서 골랐을 때
   * 센터링·선택의 대상은 group 노드이고, 조회 대상만 자식 application이 된다.
   */
  serviceGroup?: GetServerMap.NodeData;
  /** 목록에 함께 보일 소속 service 이름. 표시할 것이 없으면 undefined다. */
  serviceName?: string;
}

/**
 * 목록에 함께 보일 소속 service 이름을 정한다. 표시하지 않을 노드는 undefined.
 *
 * 노드 key가 `serviceName^applicationName^serviceType`인 노드만 자기 service를 보여준다.
 * servermap 응답의 key는 `applicationName^serviceType`뿐이라, 그 화면에서는 serviceName이
 * 채워져 있어도 화면의 service 하나를 모든 행에 되풀이해 찍는 셈이 되므로 표시하지 않는다.
 *
 * serviceName은 escape되지 않아(applicationName만 escape된다) `^`가 들어오면 토큰이 더 늘 수 있다.
 * 그래서 토큰 개수로 세지 않고 접두사로 확인한다.
 */
const resolveSearchServiceName = (node: GetServerMap.NodeData): string | undefined =>
  node.serviceName && node.key?.startsWith(`${node.serviceName}^`) ? node.serviceName : undefined;

/**
 * 노드 배열을 검색 목록 항목으로 편다. service group은 group 자체 + 소속 application 순으로 담는다.
 *
 * group 자체를 남기는 이유: service 이름으로 찾아 들어오는 기존 경로(B를 검색해 B 그룹으로 이동)를
 * 그대로 두기 위해서다. 판별은 `findServiceGroupNode`와 같이 `subNodes`로 한다 —
 * servermap/filteredMap 응답에는 없는 필드라 그 화면들에서는 노드가 1:1로 담긴다.
 *
 * 소속 service 표시 여부는 그와 별개로 노드 key에서 판단한다(`resolveSearchServiceName`).
 * servermap은 key가 2단이라 표시되지 않고, filteredMap은 `enableServiceMap`이 켜져 3단으로
 * 내려올 때 표시된다 — 그 화면의 map도 여러 service의 노드를 함께 그리므로 같은 규칙이 맞다.
 */
export const buildServerMapSearchList = (
  nodes: GetServerMap.NodeData[] | undefined,
): ServerMapSearchItem[] =>
  (nodes ?? []).flatMap((node): ServerMapSearchItem[] => {
    const subNodes = node.subNodes;

    if (!Array.isArray(subNodes) || subNodes.length === 0) {
      return [{ node, serviceName: resolveSearchServiceName(node) }];
    }

    // group 노드 자체는 이름이 곧 service라 소속 service를 따로 붙이지 않는다.
    return [
      { node, isServiceGroup: true },
      ...subNodes.map((subNode) => ({
        node: subNode,
        serviceGroup: node,
        // 자식의 service는 group 엔트리가 알려준 값이 정본이다(자식 key에서 다시 읽지 않는다).
        serviceName: node.serviceName,
      })),
    ];
  });
