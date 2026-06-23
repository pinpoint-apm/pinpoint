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
        applicationName: entry.serviceName,
        serviceType: firstNode?.serviceType ?? 'UNKNOWN',
        serviceTypeCode: firstNode?.serviceTypeCode ?? 0,
        nodeCategory: firstNode?.nodeCategory ?? GetServerMap.NodeCategory.SERVER,
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
