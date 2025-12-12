import {
  getV2Url,
  getV2ApplicationUrl,
  getV2RealtimeUrl,
  getV2ScatterRealtimeUrl,
  getV2InspectorUrl,
} from './routeV2';
import { getApplicationPath } from './route';
import { APP_PATH, ApplicationType } from '@pinpoint-fe/ui/src/constants';

// Mock route helper
jest.mock('./route', () => ({
  getApplicationPath: jest.fn((path: string) => (application?: ApplicationType) => {
    if (application?.applicationName && application?.serviceType) {
      return `${path}/${application.applicationName}@${application.serviceType}`;
    }
    return path;
  }),
}));

// Mock date utils
jest.mock('../date', () => ({
  getParsedDateRange: jest.fn(({ from, to }: { from: string; to: string }) => ({
    from: new Date(from),
    to: new Date(to),
  })),
  convertToTimeUnit: jest.fn((ms: number) => {
    if (ms < 60000) return 'm';
    if (ms < 3600000) return 'h';
    if (ms < 86400000) return 'd';
    return 'w';
  }),
}));

describe('Test routeV2 helper utils', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Test "getV2Url"', () => {
    test('Generate V2 URL with page path', () => {
      const pagePath = '/main/realtime';
      const result = getV2Url(pagePath);
      // Check that it uses location.protocol and location.host
      expect(result).toContain(location.protocol);
      expect(result).toContain(location.host);
      expect(result).toContain(pagePath);
      expect(result).toBe(`${location.protocol}//${location.host}${pagePath}`);
    });
  });

  describe('Test "getV2ApplicationUrl"', () => {
    test('Generate URL without application and query params', () => {
      const pagePath = '/main/realtime';
      const urlGenerator = getV2ApplicationUrl(pagePath);
      const result = urlGenerator();

      expect(getApplicationPath).toHaveBeenCalledWith(pagePath);
      expect(result).toContain(location.protocol);
      expect(result).toContain(location.host);
      expect(result).toContain('/main/realtime');
    });

    test('Generate URL with application', () => {
      const pagePath = '/main/realtime';
      const application = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const urlGenerator = getV2ApplicationUrl(pagePath);
      const result = urlGenerator(application);

      expect(result).toContain(location.protocol);
      expect(result).toContain(location.host);
      expect(result).toContain('/main/realtime/test-app@TOMCAT');
    });

    test('Generate URL with query params (from and to)', () => {
      const pagePath = '/main/realtime';
      const queryParams = {
        from: '2023-11-10T00:00:00Z',
        to: '2023-11-10T23:59:59Z',
      };
      const urlGenerator = getV2ApplicationUrl(pagePath);
      const result = urlGenerator(undefined, queryParams);

      expect(result).toContain(location.protocol);
      expect(result).toContain(location.host);
      expect(result).toContain('/2023-11-10T23:59:59Z');
    });

    test('Generate URL with application and query params', () => {
      const pagePath = '/main/realtime';
      const application = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const queryParams = {
        from: '2023-11-10T00:00:00Z',
        to: '2023-11-10T23:59:59Z',
      };
      const urlGenerator = getV2ApplicationUrl(pagePath);
      const result = urlGenerator(application, queryParams);

      expect(result).toContain('test-app@TOMCAT');
      expect(result).toContain('/2023-11-10T23:59:59Z');
    });

    test('Handle missing to in query params', () => {
      const pagePath = '/main/realtime';
      const queryParams = {
        from: '2023-11-10T00:00:00Z',
      };
      const urlGenerator = getV2ApplicationUrl(pagePath);
      const result = urlGenerator(undefined, queryParams as Record<string, string>);

      expect(result).not.toContain('/2023-11-10T00:00:00Z');
    });

    test('Handle missing from in query params', () => {
      const pagePath = '/main/realtime';
      const queryParams = {
        to: '2023-11-10T23:59:59Z',
      };
      const urlGenerator = getV2ApplicationUrl(pagePath);
      const result = urlGenerator(undefined, queryParams as Record<string, string>);

      expect(result).not.toContain('/2023-11-10T23:59:59Z');
    });
  });

  describe('Test "getV2RealtimeUrl"', () => {
    test('Generate realtime URL', () => {
      const application = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const result = getV2RealtimeUrl(application);

      expect(getApplicationPath).toHaveBeenCalledWith('/main/realtime');
      expect(result).toContain('test-app@TOMCAT');
      expect(result).toContain(location.protocol);
      expect(result).toContain(location.host);
    });
  });

  describe('Test "getV2ScatterRealtimeUrl"', () => {
    test('Generate scatter realtime URL', () => {
      const application = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const result = getV2ScatterRealtimeUrl(application);

      expect(getApplicationPath).toHaveBeenCalledWith(APP_PATH.SCATTER_FULL_SCREEN_REALTIME);
      expect(result).toContain('test-app@TOMCAT');
      expect(result).toContain(location.protocol);
      expect(result).toContain(location.host);
    });
  });

  describe('Test "getV2InspectorUrl"', () => {
    test('Generate inspector URL', () => {
      const application = {
        applicationName: 'test-app',
        serviceType: 'TOMCAT',
      };
      const result = getV2InspectorUrl(application);

      expect(getApplicationPath).toHaveBeenCalledWith(APP_PATH.INSPECTOR);
      expect(result).toContain('test-app@TOMCAT');
      expect(result).toContain(location.protocol);
      expect(result).toContain(location.host);
    });
  });
});
