import { getBaseNodeId, parseNodeKey, unescapeApplicationName } from './serverMap';
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

    describe('enableServiceMap=true', () => {
      test('Return empty string when node list is empty (prevents format mismatch on data load)', () => {
        const application: ApplicationType = {
          applicationName: 'test-app',
          serviceType: 'TOMCAT',
        };
        const applicationMapData: GetServerMap.ApplicationMapData = {
          range: { from: 0, to: 0, fromDateTime: '', toDateTime: '' },
          timestamp: [],
          nodeDataArray: [],
          linkDataArray: [],
        };

        const result = getBaseNodeId({ application, applicationMapData, enableServiceMap: true });
        expect(result).toBe('');
      });

      test('Return full service map node key when node exists', () => {
        const application: ApplicationType = {
          applicationName: 'test-app',
          serviceType: 'TOMCAT',
        };
        const applicationMapData: GetServerMap.ApplicationMapData = {
          range: { from: 0, to: 0, fromDateTime: '', toDateTime: '' },
          timestamp: [],
          nodeDataArray: [
            {
              key: 'default^test-app^TOMCAT',
              applicationName: 'test-app',
              serviceType: 'TOMCAT',
            } as GetServerMap.NodeData,
          ],
          linkDataArray: [],
        };

        const result = getBaseNodeId({ application, applicationMapData, enableServiceMap: true });
        expect(result).toBe('default^test-app^TOMCAT');
      });

      test('Return full key when applicationName contains escaped caret', () => {
        const application: ApplicationType = {
          applicationName: 'my^app',
          serviceType: 'TOMCAT',
        };
        const applicationMapData: GetServerMap.ApplicationMapData = {
          range: { from: 0, to: 0, fromDateTime: '', toDateTime: '' },
          timestamp: [],
          nodeDataArray: [
            {
              key: 'default^my\\^app^TOMCAT',
              applicationName: 'my^app', // already unescaped in NodeData
              serviceType: 'TOMCAT',
            } as GetServerMap.NodeData,
          ],
          linkDataArray: [],
        };

        const result = getBaseNodeId({ application, applicationMapData, enableServiceMap: true });
        expect(result).toBe('default^my\\^app^TOMCAT');
      });

      test('Return UNAUTHORIZED key when node does not exist', () => {
        const application: ApplicationType = {
          applicationName: 'test-app',
          serviceType: 'TOMCAT',
        };
        const applicationMapData: GetServerMap.ApplicationMapData = {
          range: { from: 0, to: 0, fromDateTime: '', toDateTime: '' },
          timestamp: [],
          nodeDataArray: [
            {
              key: 'default^other-app^JETTY',
              applicationName: 'other-app',
              serviceType: 'JETTY',
            } as GetServerMap.NodeData,
          ],
          linkDataArray: [],
        };

        const result = getBaseNodeId({ application, applicationMapData, enableServiceMap: true });
        expect(result).toBe('test-app^UNAUTHORIZED');
      });

      test('Return UNAUTHORIZED node key when UNAUTHORIZED node exists', () => {
        const application: ApplicationType = {
          applicationName: 'test-app',
          serviceType: 'TOMCAT',
        };
        const applicationMapData: GetServerMap.ApplicationMapData = {
          range: { from: 0, to: 0, fromDateTime: '', toDateTime: '' },
          timestamp: [],
          nodeDataArray: [
            {
              key: 'default^test-app^UNAUTHORIZED',
              applicationName: 'test-app',
              serviceType: 'UNAUTHORIZED',
            } as GetServerMap.NodeData,
          ],
          linkDataArray: [],
        };

        const result = getBaseNodeId({ application, applicationMapData, enableServiceMap: true });
        expect(result).toBe('default^test-app^UNAUTHORIZED');
      });
    });
  });

  describe('Test "unescapeApplicationName"', () => {
    test('Return unchanged string when no escapes', () => {
      expect(unescapeApplicationName('my-app')).toBe('my-app');
    });

    test('Unescape \\^ to ^', () => {
      expect(unescapeApplicationName('my\\^app')).toBe('my^app');
    });

    test('Unescape multiple escaped carets', () => {
      expect(unescapeApplicationName('a\\^b\\^c')).toBe('a^b^c');
    });

    test('Unescape \\\\ to \\', () => {
      expect(unescapeApplicationName('my\\\\app')).toBe('my\\app');
    });

    test('Unescape mixed escapes', () => {
      expect(unescapeApplicationName('a\\\\\\^b')).toBe('a\\^b');
    });

    test('Return empty string for empty input', () => {
      expect(unescapeApplicationName('')).toBe('');
    });
  });

  describe('Test "parseNodeKey"', () => {
    test('Parse 2-part key (enableServiceMap=false format)', () => {
      expect(parseNodeKey('myApp^TOMCAT')).toEqual({
        applicationName: 'myApp',
        serviceType: 'TOMCAT',
      });
    });

    test('Parse 3-part key (enableServiceMap=true format)', () => {
      expect(parseNodeKey('default^myApp^TOMCAT')).toEqual({
        serviceName: 'default',
        applicationName: 'myApp',
        serviceType: 'TOMCAT',
      });
    });

    test('Parse 3-part key with escaped caret in applicationName', () => {
      expect(parseNodeKey('default^my\\^app^TOMCAT')).toEqual({
        serviceName: 'default',
        applicationName: 'my^app',
        serviceType: 'TOMCAT',
      });
    });

    test('Parse 3-part key with multiple escaped carets in applicationName', () => {
      expect(parseNodeKey('svc^a\\^b\\^c^JETTY')).toEqual({
        serviceName: 'svc',
        applicationName: 'a^b^c',
        serviceType: 'JETTY',
      });
    });

    test('Parse 3-part key with escaped backslash before escaped caret', () => {
      expect(parseNodeKey('svc^a\\\\\\^b^TOMCAT')).toEqual({
        serviceName: 'svc',
        applicationName: 'a\\^b',
        serviceType: 'TOMCAT',
      });
    });

    test('Parse UNAUTHORIZED key (2-part)', () => {
      expect(parseNodeKey('myApp^UNAUTHORIZED')).toEqual({
        applicationName: 'myApp',
        serviceType: 'UNAUTHORIZED',
      });
    });

    test('Parse UNAUTHORIZED key (3-part)', () => {
      expect(parseNodeKey('default^myApp^UNAUTHORIZED')).toEqual({
        serviceName: 'default',
        applicationName: 'myApp',
        serviceType: 'UNAUTHORIZED',
      });
    });
  });
});
