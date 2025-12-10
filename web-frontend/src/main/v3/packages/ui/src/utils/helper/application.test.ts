import { getApplicationTypeAndName, getApplicationKey } from './application';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';

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
