import { getBaseNodeId, getTimeSeriesApdexInfo } from './serverMap';
import {
  ApplicationType,
  GetServerMap,
  FilteredMapType as FilteredMap,
} from '@pinpoint-fe/ui/src/constants';

describe('Test serverMap helper utils', () => {
  describe('Test "getTimeSeriesApdexInfo"', () => {
    const makeNode = (
      overrides: Partial<GetServerMap.NodeData> = {},
    ): GetServerMap.NodeData =>
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

    test('Handle case-insensitive matching for UNAUTHORIZED replacement', () => {
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
