import { getBaseNodeId, getTimeSeriesApdexInfo, resolveUseStatisticsAgentState } from './serverMap';
import {
  ApplicationType,
  GetServerMap,
  FilteredMapType as FilteredMap,
} from '@pinpoint-fe/ui/src/constants';

describe('Test serverMap helper utils', () => {
  describe('Test "resolveUseStatisticsAgentState"', () => {
    test('Preserve an explicitly disabled option', () => {
      expect(resolveUseStatisticsAgentState(false)).toBe(false);
    });

    test('Preserve an explicitly enabled option', () => {
      expect(resolveUseStatisticsAgentState(true)).toBe(true);
    });

    test.each([null, undefined])('Use the fallback when the option is %s', (value) => {
      expect(resolveUseStatisticsAgentState(value)).toBe(true);
    });
  });

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
});
