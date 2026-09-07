import {
  findCallerApplication,
  findLinkOfApplications,
  findNodeOfApplication,
  getBaseNodeId,
  getSelectedTargetApplication,
  getTimeSeriesApdexInfo,
  isMergedMapTarget,
  parseNodeApplication,
} from './serverMap';
import {
  ApplicationType,
  GetServerMap,
  FilteredMapType as FilteredMap,
} from '@pinpoint-fe/ui/src/constants';

describe('Test serverMap helper utils', () => {
  describe('Test "getTimeSeriesApdexInfo"', () => {
    const makeNode = (overrides: Partial<GetServerMap.NodeData> = {}): GetServerMap.NodeData =>
      ({
        isAuthorized: true,
        apdexSlot: [],
        ...overrides,
      }) as GetServerMap.NodeData;

    test('Return empty array when node is not authorized', () => {
      const node = makeNode({ isAuthorized: false, apdexSlot: [0.9, 0.8] });
      expect(getTimeSeriesApdexInfo(node)).toEqual([]);
    });

    test('Return empty array when apdexSlot is undefined', () => {
      const node = makeNode({ apdexSlot: undefined });
      expect(getTimeSeriesApdexInfo(node)).toEqual([]);
    });

    test('Return empty array when apdexSlot is empty', () => {
      const node = makeNode({ apdexSlot: [] });
      expect(getTimeSeriesApdexInfo(node)).toEqual([]);
    });

    test('Return apdexSlot as-is when length is within 24', () => {
      const slot = [0.95, 0.7, 0.5];
      const node = makeNode({ apdexSlot: slot });
      expect(getTimeSeriesApdexInfo(node)).toEqual(slot);
    });

    test('Return up to 24 entries when apdexSlot is longer (defensive)', () => {
      const slot = Array.from({ length: 30 }, (_, i) => i / 30);
      const node = makeNode({ apdexSlot: slot });
      const result = getTimeSeriesApdexInfo(node);
      expect(result).toHaveLength(24);
      expect(result).toEqual(slot.slice(0, 24));
    });

    test('Return empty array for FilteredMap.NodeData (no apdexSlot field)', () => {
      const node = { isAuthorized: true } as FilteredMap.NodeData;
      expect(getTimeSeriesApdexInfo(node)).toEqual([]);
    });

    test('Treat -1 (UNCOLLECTED_VALUE) as 1 (Excellent)', () => {
      const node = makeNode({ apdexSlot: [-1, 0.6, -1, 0.95] });
      expect(getTimeSeriesApdexInfo(node)).toEqual([1, 0.6, 1, 0.95]);
    });
  });

  describe('Test "getBaseNodeId"', () => {
    test('Return base node ID when node list is empty', () => {
      const application: ApplicationType = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const applicationMapData: GetServerMap.ApplicationMapData = {
        range: {
          from: 0,
          to: 0,
          fromDateTime: '',
          toDateTime: '',
        },
        timestamp: [],
        nodeDataArray: [],
        linkDataArray: [],
      };

      const result = getBaseNodeId({ application, applicationMapData });
      expect(result).toBe('test-app^TOMCAT');
    });

    test('Return base node ID when node exists in node list', () => {
      const application: ApplicationType = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const applicationMapData: GetServerMap.ApplicationMapData = {
        range: {
          from: 0,
          to: 0,
          fromDateTime: '',
          toDateTime: '',
        },
        timestamp: [],
        nodeDataArray: [
          { key: 'test-app^TOMCAT' } as GetServerMap.NodeData,
          { key: 'other-app^JETTY' } as GetServerMap.NodeData,
        ],
        linkDataArray: [],
      };

      const result = getBaseNodeId({ application, applicationMapData });
      expect(result).toBe('test-app^TOMCAT');
    });

    test('Return UNAUTHORIZED node ID when node does not exist in node list', () => {
      const application: ApplicationType = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const applicationMapData: GetServerMap.ApplicationMapData = {
        range: {
          from: 0,
          to: 0,
          fromDateTime: '',
          toDateTime: '',
        },
        timestamp: [],
        nodeDataArray: [{ key: 'other-app^JETTY' } as GetServerMap.NodeData],
        linkDataArray: [],
      };

      const result = getBaseNodeId({ application, applicationMapData });
      expect(result).toBe('test-app^UNAUTHORIZED');
    });

    test('Return empty string when application is null', () => {
      const application = null;
      const applicationMapData: GetServerMap.ApplicationMapData = {
        range: {
          from: 0,
          to: 0,
          fromDateTime: '',
          toDateTime: '',
        },
        timestamp: [],
        nodeDataArray: [],
        linkDataArray: [],
      };

      const result = getBaseNodeId({ application, applicationMapData });
      expect(result).toBe('');
    });

    test('Return empty string when applicationMapData is undefined', () => {
      const application: ApplicationType = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };

      const result = getBaseNodeId({ application });
      expect(result).toBe('');
    });

    test('Handle FilteredMap.ApplicationMapData type', () => {
      const application: ApplicationType = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const applicationMapData: FilteredMap.ApplicationMapData = {
        range: {
          from: 0,
          to: 0,
          fromDateTime: '',
          toDateTime: '',
        },
        timestamp: [],
        nodeDataArray: [{ key: 'test-app^TOMCAT' } as FilteredMap.NodeData],
        linkDataArray: [],
      };

      const result = getBaseNodeId({ application, applicationMapData });
      expect(result).toBe('test-app^TOMCAT');
    });

    test('Return actual 3-part node key for unauthorized serviceMap node', () => {
      // serviceMap 응답: key는 3-part(serviceName^app^serviceType), nodeKey는 2-part.
      // 권한 없는 노드는 serviceType이 UNAUTHORIZED로 치환되어 내려온다.
      const application: ApplicationType = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const applicationMapData: GetServerMap.ApplicationMapData = {
        range: {
          from: 0,
          to: 0,
          fromDateTime: '',
          toDateTime: '',
        },
        timestamp: [],
        nodeDataArray: [
          {
            key: 'my-service^test-app^UNAUTHORIZED',
            nodeKey: 'test-app^UNAUTHORIZED',
          } as GetServerMap.NodeData,
        ],
        linkDataArray: [],
      };

      const result = getBaseNodeId({ application, applicationMapData });
      // 합성한 2-part('test-app^UNAUTHORIZED')가 아니라 실제 노드 key(3-part)를 반환해야
      // cytoscape id와 일치하여 센터링이 동작한다.
      expect(result).toBe('my-service^test-app^UNAUTHORIZED');
    });

    test('Return node key when 2-part unauthorized node exists in serverMap response', () => {
      // 레거시 serverMap 응답: key가 2-part(app^serviceType)이고 nodeKey가 없다.
      // 권한 없는 노드(app^UNAUTHORIZED)가 목록에 존재하면 합성한 키와 동일한
      // 실제 node.key를 반환하여 기존 동작과 호환됨을 보장한다.
      const application: ApplicationType = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const applicationMapData: GetServerMap.ApplicationMapData = {
        range: {
          from: 0,
          to: 0,
          fromDateTime: '',
          toDateTime: '',
        },
        timestamp: [],
        nodeDataArray: [{ key: 'test-app^UNAUTHORIZED' } as GetServerMap.NodeData],
        linkDataArray: [],
      };

      const result = getBaseNodeId({ application, applicationMapData });
      expect(result).toBe('test-app^UNAUTHORIZED');
    });

    test('Return group node key when unauthorized node is a child of a service group', () => {
      // 권한 없는 노드가 service group의 자식인 경우: 그래프에는 그룹 노드만 그려지므로
      // subNodes 안의 UNAUTHORIZED 키를 찾아 그룹 노드의 key를 base로 반환해야 한다.
      const application: ApplicationType = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const applicationMapData: GetServerMap.ApplicationMapData = {
        range: {
          from: 0,
          to: 0,
          fromDateTime: '',
          toDateTime: '',
        },
        timestamp: [],
        nodeDataArray: [
          {
            key: 'my-service-group^UNAUTHORIZED',
            subNodes: [
              {
                key: 'my-service^test-app^UNAUTHORIZED',
                nodeKey: 'test-app^UNAUTHORIZED',
              } as GetServerMap.NodeData,
            ],
          } as GetServerMap.NodeData,
        ],
        linkDataArray: [],
      };

      const result = getBaseNodeId({ application, applicationMapData });
      expect(result).toBe('my-service-group^UNAUTHORIZED');
    });

    test('Return UNAUTHORIZED key when serviceType case does not match (matching is case-sensitive)', () => {
      const application: ApplicationType = {
        applicationName: 'test-app',
        serviceType: 'tomcat', // lowercase
      };
      const applicationMapData: GetServerMap.ApplicationMapData = {
        range: {
          from: 0,
          to: 0,
          fromDateTime: '',
          toDateTime: '',
        },
        timestamp: [],
        nodeDataArray: [
          { key: 'test-app^TOMCAT' } as GetServerMap.NodeData, // uppercase
        ],
        linkDataArray: [],
      };

      const result = getBaseNodeId({ application, applicationMapData });
      expect(result).toBe('test-app^UNAUTHORIZED');
    });
  });

  describe('Test "parseNodeApplication"', () => {
    // servermap 응답의 node/link id.
    test('Read a 2-part id (servermap)', () => {
      expect(parseNodeApplication('ACL-PORTAL-DEV^SPRING_BOOT')).toEqual({
        applicationName: 'ACL-PORTAL-DEV',
        serviceType: 'SPRING_BOOT',
      });
    });

    // servicemap 응답의 node/link id. 앞에 serviceName이 붙는다(백엔드 `ServiceNodeName`).
    test('Read a 3-part id (servicemap) without the service name leaking into the application', () => {
      expect(parseNodeApplication('DEFAULT^ACL-PORTAL-DEV^SPRING_BOOT')).toEqual({
        applicationName: 'ACL-PORTAL-DEV',
        serviceType: 'SPRING_BOOT',
      });
      expect(parseNodeApplication('blogService^ACL-PORTAL-DEV^SPRING_BOOT')).toEqual({
        applicationName: 'ACL-PORTAL-DEV',
        serviceType: 'SPRING_BOOT',
      });
    });

    // 3단 id의 applicationName은 백엔드가 '^'를 escape해서 싣는다(`ApplicationNameEscaper`).
    // 단순히 '^'로 쪼개면 escape된 구분자를 진짜 구분자로 읽어 이름이 잘린다.
    test('Keep an escaped delimiter inside the application name', () => {
      expect(parseNodeApplication('DEFAULT^a\\^b^TOMCAT')).toEqual({
        applicationName: 'a^b',
        serviceType: 'TOMCAT',
      });
      expect(parseNodeApplication('blogService^a\\^b\\^c^TOMCAT')).toEqual({
        applicationName: 'a^b^c',
        serviceType: 'TOMCAT',
      });
    });

    // escape 문자 자체도 escape되어 온다('\\' → '\\\\').
    test('Unescape an escaped backslash', () => {
      expect(parseNodeApplication('DEFAULT^a\\\\b^TOMCAT')).toEqual({
        applicationName: 'a\\b',
        serviceType: 'TOMCAT',
      });
    });

    // 2단은 백엔드가 escape하지 않으므로(`NodeName.newNodeKey`) 되돌리지 않는다.
    test('Leave a 2-part application name untouched', () => {
      expect(parseNodeApplication('a\\b^TOMCAT')).toEqual({
        applicationName: 'a\\b',
        serviceType: 'TOMCAT',
      });
    });

    // servicemap의 service group(접힌 service) 노드·링크는 id가 serviceName 하나뿐이다.
    test('Return null when the id carries no application', () => {
      expect(parseNodeApplication('blogService')).toBeNull();
      expect(parseNodeApplication('')).toBeNull();
      expect(parseNodeApplication()).toBeNull();
      expect(parseNodeApplication('ACL-PORTAL-DEV^')).toBeNull();
      expect(parseNodeApplication('^SPRING_BOOT')).toBeNull();
    });
  });

  describe('Test "findNodeOfApplication"', () => {
    const application: ApplicationType = {
      applicationName: 'ACL-PORTAL-DEV',
      serviceType: 'SPRING_BOOT',
    };
    const makeNodes = (keys: string[]) =>
      keys.map((key) => ({ key, applicationName: 'ACL-PORTAL-DEV' }) as GetServerMap.NodeData);

    // filterServerMap은 enableServiceMap 설정에 따라 2단/3단으로 갈린다
    // (`NodeRender.detailedRender`). 두 형식 모두에서 기준 노드를 찾아야 한다.
    test('Find the node whichever key format the API used', () => {
      expect(
        findNodeOfApplication(makeNodes(['ACL-PORTAL-DEV^SPRING_BOOT']), application)?.key,
      ).toBe('ACL-PORTAL-DEV^SPRING_BOOT');
      expect(
        findNodeOfApplication(makeNodes(['DEFAULT^ACL-PORTAL-DEV^SPRING_BOOT']), application)?.key,
      ).toBe('DEFAULT^ACL-PORTAL-DEV^SPRING_BOOT');
      expect(
        findNodeOfApplication(makeNodes(['blogService^ACL-PORTAL-DEV^SPRING_BOOT']), application)
          ?.key,
      ).toBe('blogService^ACL-PORTAL-DEV^SPRING_BOOT');
    });

    // 권한 없는 노드는 백엔드가 serviceType을 UNAUTHORIZED로 치환해 내려준다.
    test('Match an unauthorized node by name alone', () => {
      const nodes = [
        {
          key: 'DEFAULT^ACL-PORTAL-DEV^UNAUTHORIZED',
          applicationName: 'ACL-PORTAL-DEV',
          serviceType: 'UNAUTHORIZED',
        } as GetServerMap.NodeData,
      ];

      expect(findNodeOfApplication(nodes, application)?.serviceType).toBe('UNAUTHORIZED');
    });

    test('Return undefined when nothing matches', () => {
      expect(findNodeOfApplication(makeNodes(['OTHER^TOMCAT']), application)).toBeUndefined();
      expect(findNodeOfApplication(undefined, application)).toBeUndefined();
      expect(
        findNodeOfApplication(makeNodes(['ACL-PORTAL-DEV^SPRING_BOOT']), null),
      ).toBeUndefined();
    });

    // service group 노드는 특정 application을 가리키지 않는다.
    test('Never match a service group node', () => {
      expect(findNodeOfApplication(makeNodes(['blogService']), application)).toBeUndefined();
    });
  });

  describe('Test "findLinkOfApplications"', () => {
    const from: ApplicationType = { applicationName: 'FRONT', serviceType: 'TOMCAT' };
    const to: ApplicationType = { applicationName: 'ACL-PORTAL-DEV', serviceType: 'SPRING_BOOT' };
    const makeLinks = (keys: string[]) => keys.map((key) => ({ key }) as GetServerMap.LinkData);

    test('Find the link whichever key format the API used', () => {
      expect(
        findLinkOfApplications(makeLinks(['FRONT^TOMCAT~ACL-PORTAL-DEV^SPRING_BOOT']), from, to)
          ?.key,
      ).toBe('FRONT^TOMCAT~ACL-PORTAL-DEV^SPRING_BOOT');
      expect(
        findLinkOfApplications(
          makeLinks(['DEFAULT^FRONT^TOMCAT~DEFAULT^ACL-PORTAL-DEV^SPRING_BOOT']),
          from,
          to,
        )?.key,
      ).toBe('DEFAULT^FRONT^TOMCAT~DEFAULT^ACL-PORTAL-DEV^SPRING_BOOT');
    });

    // 방향이 뒤바뀐 링크를 같은 것으로 보면 안 된다.
    test('Respect the link direction', () => {
      expect(
        findLinkOfApplications(makeLinks(['FRONT^TOMCAT~ACL-PORTAL-DEV^SPRING_BOOT']), to, from),
      ).toBeUndefined();
    });

    test('Return undefined when nothing matches', () => {
      expect(findLinkOfApplications(makeLinks(['A^TOMCAT~B^TOMCAT']), from, to)).toBeUndefined();
      expect(findLinkOfApplications(undefined, from, to)).toBeUndefined();
      // service group 링크(양쪽이 serviceName 하나뿐)
      expect(
        findLinkOfApplications(makeLinks(['blogService~shopService']), from, to),
      ).toBeUndefined();
    });
  });

  // 그래프가 여러 노드를 하나로 묶어 그린 merged 노드는 기준 application이 없다.
  // 그 자리에 있는 이름은 그래프가 붙인 라벨("total: 2")이라, 그대로 조회에 쓰면 그 이름으로
  // 요청이 나간다. servermap은 경로의 application이 기준이 되어 가려져 있었고, 경로에
  // application이 없는 servicemap에서 드러났다. (이슈 #10587)
  describe('Test "isMergedMapTarget" / "getSelectedTargetApplication"', () => {
    const mergedNodeTarget = {
      // merge가 붙이는 합성 id. map 데이터에 없으므로 currentTargetData가 잡히지 않는다.
      id: 'a-1^TOMCAT_MergeSingleNodesByServerMap^MONGO',
      type: 'node' as const,
      applicationName: 'total: 2',
      serviceType: 'MONGO',
      nodes: [{ id: 'a-mongo-1^MONGO' }, { id: 'a-mongo-2^MONGO' }],
    };

    const pickedFromMergedList = {
      // 목록에서 자식을 고르면 목록을 계속 보여주려고 nodes는 남고 id만 실제 key로 바뀐다.
      ...mergedNodeTarget,
      id: 'a-mongo-1^MONGO',
      applicationName: 'a-mongo-1',
      serviceType: 'MONGO',
    };

    const mongoNodeData = {
      key: 'a-mongo-1^MONGO',
      applicationName: 'a-mongo-1',
      serviceType: 'MONGO',
    } as GetServerMap.NodeData;

    test('merged 노드는 merged로 판별하고 기준 application을 주지 않는다', () => {
      expect(isMergedMapTarget(mergedNodeTarget, undefined)).toBe(true);
      expect(getSelectedTargetApplication(mergedNodeTarget, undefined)).toBeUndefined();
    });

    test('목록에서 자식을 고르면 그 노드가 기준이 된다', () => {
      expect(isMergedMapTarget(pickedFromMergedList, mongoNodeData)).toBe(false);
      expect(getSelectedTargetApplication(pickedFromMergedList, mongoNodeData)).toEqual({
        applicationName: 'a-mongo-1',
        serviceType: 'MONGO',
      });
    });

    test('평범한 노드는 그 노드가 기준이다', () => {
      expect(
        getSelectedTargetApplication(
          { type: 'node', applicationName: 'a-1', serviceType: 'TOMCAT' },
          mongoNodeData,
        ),
      ).toEqual({ applicationName: 'a-1', serviceType: 'TOMCAT' });
    });

    test('링크는 출발지 노드가 기준이다', () => {
      const linkData = {
        sourceInfo: { applicationName: 'a-1', serviceType: 'TOMCAT' },
        targetInfo: { applicationName: 'a-2', serviceType: 'TOMCAT' },
      } as GetServerMap.LinkData;

      expect(getSelectedTargetApplication({ type: 'edge' }, linkData)).toEqual({
        applicationName: 'a-1',
        serviceType: 'TOMCAT',
      });
    });

    test('merged 링크도 기준 application을 주지 않는다', () => {
      const mergedEdgeTarget = { type: 'edge' as const, edges: [{ target: 'a-mongo-1^MONGO' }] };

      expect(isMergedMapTarget(mergedEdgeTarget, undefined)).toBe(true);
      expect(getSelectedTargetApplication(mergedEdgeTarget, undefined)).toBeUndefined();
    });

    test('고른 것이 없으면 기준도 없다', () => {
      expect(isMergedMapTarget(undefined, undefined)).toBe(false);
      expect(getSelectedTargetApplication(undefined, undefined)).toBeUndefined();
    });

    // 통계 API는 applicationName/serviceTypeName을 "상대편(기준) application"으로 쓴다.
    // DB처럼 자기 agent가 없는 리프 노드는 호출자 링크에서 수치가 나오므로, 기준이 노드 자신이면
    // 백엔드가 그 노드를 map 중심으로 놓는 다른 분기로 빠진다. servermap은 경로의 application이
    // 그 자리를 맡지만 경로에 application이 없는 servicemap에는 그것이 없다. (이슈 #10587)
    describe('merged 묶음에서 고른 노드의 기준 application', () => {
      const inboundLink = {
        to: 'A^a-mongo-1^MONGO',
        sourceInfo: { applicationName: 'a-1', serviceType: 'TOMCAT' },
      } as GetServerMap.LinkData;

      const pickedNodeData = { key: 'A^a-mongo-1^MONGO' } as GetServerMap.NodeData;

      const pickedTarget = {
        type: 'node' as const,
        applicationName: 'a-mongo-1',
        serviceType: 'MONGO',
        nodes: [{ id: 'A^a-mongo-1^MONGO' }, { id: 'A^a-mongo-2^MONGO' }],
      };

      test('들어오는 링크가 하나면 그 출발지가 기준이다', () => {
        expect(getSelectedTargetApplication(pickedTarget, pickedNodeData, [inboundLink])).toEqual({
          applicationName: 'a-1',
          serviceType: 'TOMCAT',
        });
      });

      // 어느 쪽을 기준으로 삼을지 정할 수 없으면 기존 기준(노드 자신)을 그대로 쓴다.
      test('들어오는 링크가 여럿이면 노드 자신이 기준이다', () => {
        const anotherInbound = {
          to: 'A^a-mongo-1^MONGO',
          sourceInfo: { applicationName: 'a-2', serviceType: 'TOMCAT' },
        } as GetServerMap.LinkData;

        expect(
          getSelectedTargetApplication(pickedTarget, pickedNodeData, [inboundLink, anotherInbound]),
        ).toEqual({ applicationName: 'a-mongo-1', serviceType: 'MONGO' });
      });

      test('링크 정보가 없으면 노드 자신이 기준이다', () => {
        expect(getSelectedTargetApplication(pickedTarget, pickedNodeData)).toEqual({
          applicationName: 'a-mongo-1',
          serviceType: 'MONGO',
        });
      });

      // merged 묶음에서 고른 것이 아니면 호출자를 찾지 않는다(그 노드를 직접 열어 본 것과 같아야 한다).
      test('merged 묶음이 아니면 호출자를 기준으로 삼지 않는다', () => {
        expect(
          getSelectedTargetApplication(
            { type: 'node', applicationName: 'a-mongo-1', serviceType: 'MONGO' },
            pickedNodeData,
            [inboundLink],
          ),
        ).toEqual({ applicationName: 'a-mongo-1', serviceType: 'MONGO' });
      });

      test('findCallerApplication은 노드 key가 없으면 undefined다', () => {
        expect(findCallerApplication([inboundLink], undefined)).toBeUndefined();
        expect(findCallerApplication(undefined, 'A^a-mongo-1^MONGO')).toBeUndefined();
        expect(findCallerApplication([inboundLink], 'A^a-mongo-9^MONGO')).toBeUndefined();
      });
    });
  });
});
