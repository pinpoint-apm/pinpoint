import { getBaseNodeId } from './serverMap';
import {
  ApplicationType,
  GetServerMap,
  FilteredMapType as FilteredMap,
} from '@pinpoint-fe/ui/src/constants';

describe('Test serverMap helper utils', () => {
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
