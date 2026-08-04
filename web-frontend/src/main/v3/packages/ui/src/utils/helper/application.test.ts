import {
  getApplicationTypeAndName,
  getApplicationKey,
  getApplicationTypeAndNameFromPath,
  getServiceAndApplicationTypeAndName,
  getServiceNameFromPath,
  hasServiceNameInPath,
} from './application';
import { APP_PATH, ApplicationType } from '@pinpoint-fe/ui/src/constants';

describe('Test application helper utils', () => {
  describe('Test "getApplicationTypeAndName"', () => {
    test('Extract application name and service type from valid path', () => {
      const path = '/appName@serviceType';
      const result = getApplicationTypeAndName(path);
      expect(result).toEqual({
        applicationName: 'appName',
        serviceType: 'serviceType',
      });
    });

    test('Extract application name and service type from path with caret separator', () => {
      const path = '/appName^serviceType';
      const result = getApplicationTypeAndName(path);
      expect(result).toEqual({
        applicationName: 'appName',
        serviceType: 'serviceType',
      });
    });

    test('Extract application name and service type from path without leading slash', () => {
      const path = 'appName@serviceType';
      const result = getApplicationTypeAndName(path);
      expect(result).toEqual({
        applicationName: 'appName',
        serviceType: 'serviceType',
      });
    });

    test('Return null when path does not match pattern', () => {
      const path = '/invalid-path';
      const result = getApplicationTypeAndName(path);
      expect(result).toBeNull();
    });

    test('Return null when path is empty', () => {
      const path = '';
      const result = getApplicationTypeAndName(path);
      expect(result).toBeNull();
    });

    test('Return null when path has no separator', () => {
      const path = '/appName';
      const result = getApplicationTypeAndName(path);
      expect(result).toBeNull();
    });

    test('Handle path with multiple segments', () => {
      const path = '/parent/appName@serviceType';
      const result = getApplicationTypeAndName(path);
      expect(result).toEqual({
        applicationName: 'appName',
        serviceType: 'serviceType',
      });
    });
  });

  describe('Test "hasServiceNameInPath"', () => {
    test.each([
      [`${APP_PATH.TRANSACTION_LIST}`, true],
      [`${APP_PATH.TRANSACTION_LIST}/svc@appName@TOMCAT`, true],
      [`${APP_PATH.TRANSACTION_DETAIL}`, true],
      [`${APP_PATH.TRANSACTION_DETAIL}/svc@appName@TOMCAT`, true],
      [`${APP_PATH.SERVICE_MAP}/appName@TOMCAT`, false],
      [`${APP_PATH.SERVER_MAP}/appName@TOMCAT`, false],
      ['', false],
    ])('Return %p → %p', (pathname, expected) => {
      expect(hasServiceNameInPath(pathname)).toBe(expected);
    });
  });

  describe('Test "getServiceAndApplicationTypeAndName"', () => {
    test('Split service name, application name and service type', () => {
      const result = getServiceAndApplicationTypeAndName('/transactionList/svc@appName@TOMCAT');
      expect(result).toEqual({
        serviceName: 'svc',
        applicationName: 'appName',
        serviceType: 'TOMCAT',
      });
    });

    test('Return undefined service name for the legacy two part segment', () => {
      const result = getServiceAndApplicationTypeAndName('/transactionList/appName@TOMCAT');
      expect(result).toEqual({
        serviceName: undefined,
        applicationName: 'appName',
        serviceType: 'TOMCAT',
      });
    });

    test('Keep "@" inside the application name after the service name', () => {
      const result = getServiceAndApplicationTypeAndName('/transactionList/svc@app@Name@TOMCAT');
      expect(result).toEqual({
        serviceName: 'svc',
        applicationName: 'app@Name',
        serviceType: 'TOMCAT',
      });
    });

    test('Return null when the segment does not match the pattern', () => {
      expect(getServiceAndApplicationTypeAndName('/transactionList/appName')).toBeNull();
      expect(getServiceAndApplicationTypeAndName('')).toBeNull();
    });
  });

  describe('Test "getApplicationTypeAndNameFromPath"', () => {
    test('Drop the service name on a path that carries it', () => {
      const result = getApplicationTypeAndNameFromPath('/transactionList/svc@appName@TOMCAT');
      expect(result).toEqual({ applicationName: 'appName', serviceType: 'TOMCAT' });
    });

    test('Keep "@" in the application name on a path that carries no service name', () => {
      const result = getApplicationTypeAndNameFromPath('/serverMap/svc@appName@TOMCAT');
      expect(result).toEqual({ applicationName: 'svc@appName', serviceType: 'TOMCAT' });
    });

    test('Return null when the path has no application segment', () => {
      expect(getApplicationTypeAndNameFromPath('/transactionList')).toBeNull();
      expect(getApplicationTypeAndNameFromPath('/serverMap')).toBeNull();
    });
  });

  describe('Test "getServiceNameFromPath"', () => {
    test('Return the service name on a path that carries it', () => {
      expect(getServiceNameFromPath('/transactionList/svc@appName@TOMCAT')).toBe('svc');
    });

    test('Return undefined on a legacy path without a service name', () => {
      expect(getServiceNameFromPath('/transactionList/appName@TOMCAT')).toBeUndefined();
    });

    test('Return undefined on a path that does not carry a service name', () => {
      expect(getServiceNameFromPath('/serverMap/svc@appName@TOMCAT')).toBeUndefined();
      expect(getServiceNameFromPath('/serviceMap/svc@appName@TOMCAT')).toBeUndefined();
    });

    test('Read the service name on the transactionDetail path too', () => {
      expect(getServiceNameFromPath('/transactionDetail/svc@appName@TOMCAT')).toBe('svc');
      expect(getServiceNameFromPath('/transactionDetail/appName@TOMCAT')).toBeUndefined();
    });

    // `getServiceScopedApplicationPath`가 인코딩해서 넣은 값을 되돌린다.
    test('Decode the encoded service name', () => {
      expect(getServiceNameFromPath('/transactionList/a%2Fb@appName@TOMCAT')).toBe('a/b');
      expect(getServiceNameFromPath('/transactionList/a%40b@appName@TOMCAT')).toBe('a@b');
      expect(getServiceNameFromPath('/transactionList/a%20b@appName@TOMCAT')).toBe('a b');
    });

    // 경로는 사용자가 직접 편집할 수 있다. 렌더 중에 호출되므로 던지면 화면이 죽는다.
    test('Fall back to the raw value on a malformed encoding instead of throwing', () => {
      expect(getServiceNameFromPath('/transactionList/100%@appName@TOMCAT')).toBe('100%');
    });
  });

  describe('Test "getApplicationKey"', () => {
    test('Return application key from application object', () => {
      const application: ApplicationType = {
        applicationName: 'appName',
        serviceType: 'serviceType',
      };
      const result = getApplicationKey(application);
      expect(result).toBe('appName^serviceType');
    });

    test('Return application key with empty strings when application is undefined', () => {
      const result = getApplicationKey(undefined);
      expect(result).toBe('undefined^undefined');
    });

    test('Return application key with partial data', () => {
      const application: Partial<ApplicationType> = {
        applicationName: 'appName',
      };
      const result = getApplicationKey(application as ApplicationType);
      expect(result).toBe('appName^undefined');
    });

    test('Handle application with special characters', () => {
      const application: ApplicationType = {
        applicationName: 'app-name',
        serviceType: 'service_type',
      };
      const result = getApplicationKey(application);
      expect(result).toBe('app-name^service_type');
    });
  });
});
