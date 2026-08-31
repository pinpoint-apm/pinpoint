import {
  buildServerMapSearchList,
  findServiceGroupLink,
  findServiceGroupNode,
  flattenServiceMapResponse,
} from './serviceMap';
import { GetServerMap, GetServiceMap } from '@pinpoint-fe/ui/src/constants';

const makeAppNode = (overrides: Partial<GetServiceMap.AppNode> = {}): GetServiceMap.AppNode =>
  ({
    type: 'app',
    key: 'app^WAS',
    applicationName: 'app',
    serviceType: 'TOMCAT',
    serviceTypeCode: 1010,
    nodeCategory: GetServerMap.NodeCategory.SERVER,
    serviceKey: 'svc',
    serviceName: 'svc',
    isQueue: false,
    isAuthorized: true,
    totalCount: 0,
    errorCount: 0,
    slowCount: 0,
    hasAlert: false,
    ...overrides,
  }) as GetServiceMap.AppNode;

const makeAppLink = (overrides: Partial<GetServerMap.LinkData> = {}): GetServerMap.LinkData =>
  ({
    key: 'from~to',
    from: 'from',
    to: 'to',
    totalCount: 0,
    errorCount: 0,
    slowCount: 0,
    hasAlert: false,
    ...overrides,
  }) as GetServerMap.LinkData;

const makeResponse = (
  nodeDataArray: GetServiceMap.NodeEntry[],
  linkDataArray: GetServiceMap.LinkEntry[] = [],
): GetServiceMap.Response => ({
  applicationMapData: {
    range: { from: 1, to: 2 } as GetServerMap.Range,
    timestamp: [1, 2, 3],
    nodeDataArray,
    linkDataArray,
  },
});

describe('flattenServiceMapResponse', () => {
  test('returns undefined when data is undefined', () => {
    expect(flattenServiceMapResponse(undefined)).toBeUndefined();
  });

  test('passes through non-service (app) nodes unchanged', () => {
    const appNode = makeAppNode({ key: 'app^WAS', applicationName: 'app' });
    const result = flattenServiceMapResponse(makeResponse([appNode]));

    expect(result?.applicationMapData.nodeDataArray).toHaveLength(1);
    expect(result?.applicationMapData.nodeDataArray[0]).toBe(appNode);
  });

  test('collapses a service group into a single node and aggregates child metrics', () => {
    const child1 = makeAppNode({
      key: 'svc^a^WAS',
      serviceType: 'TOMCAT',
      serviceTypeCode: 1010,
      totalCount: 10,
      errorCount: 1,
      slowCount: 2,
      hasAlert: false,
    });
    const child2 = makeAppNode({
      key: 'svc^b^WAS',
      serviceType: 'IGNORED_FOR_GROUP', // only the first child's serviceType is used
      totalCount: 5,
      errorCount: 3,
      slowCount: 0,
      hasAlert: true,
    });
    const group: GetServiceMap.ServiceGroupNode = {
      key: 'svc-group',
      type: 'service',
      serviceName: 'my-service',
      nodes: [child1, child2],
    };

    const result = flattenServiceMapResponse(makeResponse([group]));
    const node = result?.applicationMapData.nodeDataArray[0];

    expect(result?.applicationMapData.nodeDataArray).toHaveLength(1);
    expect(node?.key).toBe('svc-group');
    expect(node?.applicationName).toBe('my-service');
    // serviceType / serviceTypeCode come from the first child node
    expect(node?.serviceType).toBe('TOMCAT');
    expect(node?.serviceTypeCode).toBe(1010);
    // counts are summed across all children
    expect(node?.totalCount).toBe(15);
    expect(node?.errorCount).toBe(4);
    expect(node?.slowCount).toBe(2);
    // hasAlert is true when ANY child has an alert
    expect(node?.hasAlert).toBe(true);
    // instanceCount reflects the number of collapsed children
    expect(node?.instanceCount).toBe(2);
    expect(node?.isAuthorized).toBe(true);
    // original children are preserved under subNodes for the popup list
    expect(node?.subNodes).toEqual([child1, child2]);
    // aggregated detail metrics are intentionally emptied on the group node
    expect(node?.apdex?.apdexScore).toBe(0);
    expect(node?.histogram).toEqual({ '1s': 0, '3s': 0, '5s': 0, Slow: 0, Error: 0 });
    expect(node?.timeSeriesHistogram).toEqual([]);
  });

  test('falls back to safe defaults when a service group has no child nodes', () => {
    const group: GetServiceMap.ServiceGroupNode = {
      key: 'empty-group',
      type: 'service',
      serviceName: 'empty',
      nodes: [],
    };

    const node = flattenServiceMapResponse(makeResponse([group]))?.applicationMapData
      .nodeDataArray[0];

    expect(node?.serviceType).toBe('UNKNOWN');
    expect(node?.serviceTypeCode).toBe(0);
    expect(node?.nodeCategory).toBe(GetServerMap.NodeCategory.SERVER);
    expect(node?.instanceCount).toBe(0);
    expect(node?.totalCount).toBe(0);
    expect(node?.hasAlert).toBe(false);
  });

  test('passes through non-service (app) links unchanged', () => {
    const appLink = makeAppLink({ key: 'a~b' });
    const result = flattenServiceMapResponse(makeResponse([], [appLink as GetServiceMap.AppLink]));

    expect(result?.applicationMapData.linkDataArray).toHaveLength(1);
    expect(result?.applicationMapData.linkDataArray[0]).toBe(appLink);
  });

  test('collapses a service group link and aggregates child link metrics', () => {
    const inner1 = makeAppLink({
      key: 'l1',
      totalCount: 7,
      errorCount: 2,
      slowCount: 1,
      hasAlert: false,
      sourceInfo: { foo: 'bar' } as unknown as GetServerMap.LinkData['sourceInfo'],
    });
    const inner2 = makeAppLink({
      key: 'l2',
      totalCount: 3,
      errorCount: 0,
      slowCount: 4,
      hasAlert: true,
    });
    const groupLink: GetServiceMap.ServiceGroupLink = {
      key: 'group-link',
      from: 'svcA',
      to: 'svcB',
      type: 'service',
      links: [inner1, inner2],
    };

    const link = flattenServiceMapResponse(makeResponse([], [groupLink]))?.applicationMapData
      .linkDataArray[0];

    expect(link?.key).toBe('group-link');
    expect(link?.from).toBe('svcA');
    expect(link?.to).toBe('svcB');
    expect(link?.totalCount).toBe(10);
    expect(link?.errorCount).toBe(2);
    expect(link?.slowCount).toBe(5);
    expect(link?.hasAlert).toBe(true);
    // sourceInfo is taken from the first child link
    expect(link?.sourceInfo).toEqual({ foo: 'bar' });
    expect((link as GetServerMap.LinkData & { subLinks?: unknown }).subLinks).toEqual([
      inner1,
      inner2,
    ]);
  });

  test('preserves range and timestamp from the original response', () => {
    const result = flattenServiceMapResponse(makeResponse([makeAppNode()]));

    expect(result?.applicationMapData.range).toEqual({ from: 1, to: 2 });
    expect(result?.applicationMapData.timestamp).toEqual([1, 2, 3]);
  });
});

describe('findServiceGroupNode / findServiceGroupLink', () => {
  const groupNode = {
    key: 'svcA',
    subNodes: [makeAppNode()],
  } as unknown as GetServerMap.NodeData;
  const appNode = { key: 'svcA^app^TOMCAT' } as GetServerMap.NodeData;

  const groupLink = {
    key: 'svcA~svcB',
    subLinks: [makeAppLink()],
  } as unknown as GetServerMap.LinkData;
  const appLink = { key: 'svcA^a^TOMCAT~svcA^b^TOMCAT' } as GetServerMap.LinkData;

  test('finds the collapsed service node by key', () => {
    expect(findServiceGroupNode([appNode, groupNode], 'svcA')).toBe(groupNode);
  });

  // 3단 id를 가진 application 노드는 group이 아니다. 우클릭 메뉴가 그대로 떠야 한다.
  test('returns undefined for an application node', () => {
    expect(findServiceGroupNode([appNode, groupNode], 'svcA^app^TOMCAT')).toBeUndefined();
  });

  test('finds the link that touches a collapsed service', () => {
    expect(findServiceGroupLink([appLink, groupLink], 'svcA~svcB')).toBe(groupLink);
  });

  // Application→Application 링크만 filteredMap으로 연결된다.
  test('returns undefined for an application to application link', () => {
    expect(
      findServiceGroupLink([appLink, groupLink], 'svcA^a^TOMCAT~svcA^b^TOMCAT'),
    ).toBeUndefined();
  });

  // servermap/filteredMap 응답에는 subNodes/subLinks가 없어 동작이 달라지지 않는다.
  test('returns undefined when the map carries no group data', () => {
    expect(findServiceGroupNode([appNode], 'svcA^app^TOMCAT')).toBeUndefined();
    expect(findServiceGroupLink([appLink], 'svcA^a^TOMCAT~svcA^b^TOMCAT')).toBeUndefined();
    expect(findServiceGroupNode(undefined, 'svcA')).toBeUndefined();
    expect(findServiceGroupLink(undefined, 'svcA~svcB')).toBeUndefined();
  });
});

describe('buildServerMapSearchList', () => {
  // servicemap 응답의 application 노드. key가 3단이라 자기 service를 알아볼 수 있다.
  const appNode = {
    key: 'svcA^a-1^TOMCAT',
    applicationName: 'a-1',
    serviceName: 'svcA',
  } as GetServerMap.NodeData;

  const makeGroup = (subNodes: GetServerMap.NodeData[]) =>
    ({
      key: 'svcB',
      applicationName: 'svcB',
      serviceName: 'svcB',
      subNodes,
    }) as unknown as GetServerMap.NodeData;

  test('returns an empty list when there are no nodes', () => {
    expect(buildServerMapSearchList(undefined)).toEqual([]);
    expect(buildServerMapSearchList([])).toEqual([]);
  });

  // servicemap의 application 노드는 group에 묶여 있지 않아도 소속 service를 보여준다.
  test('labels an application node with its own service', () => {
    expect(buildServerMapSearchList([appNode])).toEqual([{ node: appNode, serviceName: 'svcA' }]);
  });

  // servermap 응답의 key는 2단이다. serviceName이 채워져 있어도 화면의 service 하나를
  // 모든 행에 되풀이해 찍는 셈이라 표시하지 않는다.
  test('does not label a node whose key carries no service', () => {
    const serverMapNode = {
      key: 'a-1^TOMCAT',
      applicationName: 'a-1',
      serviceName: 'DEFAULT',
    } as GetServerMap.NodeData;

    expect(buildServerMapSearchList([serverMapNode])).toEqual([
      { node: serverMapNode, serviceName: undefined },
    ]);
  });

  // 이슈 #10540 — service에 묶인 application(b-1, b-2)도 검색 목록에 있어야 한다.
  test('expands a service group into the group itself and its applications', () => {
    const b1 = { key: 'svcB^b-1^TOMCAT', applicationName: 'b-1' } as GetServerMap.NodeData;
    const b2 = { key: 'svcB^b-2^TOMCAT', applicationName: 'b-2' } as GetServerMap.NodeData;
    const group = makeGroup([b1, b2]);

    expect(buildServerMapSearchList([appNode, group])).toEqual([
      { node: appNode, serviceName: 'svcA' },
      // group 항목은 이름이 곧 service다. serviceType(합성된 자식 타입)도, 소속 service도
      // 표시하지 않는다.
      { node: group, isServiceGroup: true },
      { node: b1, serviceGroup: group, serviceName: 'svcB' },
      { node: b2, serviceGroup: group, serviceName: 'svcB' },
    ]);
  });

  // 자식이 없으면 group으로 볼 수 없다(findServiceGroupNode와 같은 판별).
  test('treats a node with an empty subNodes array as a plain node', () => {
    const emptyGroup = makeGroup([]);

    expect(buildServerMapSearchList([emptyGroup])).toEqual([
      { node: emptyGroup, serviceName: undefined },
    ]);
  });
});
