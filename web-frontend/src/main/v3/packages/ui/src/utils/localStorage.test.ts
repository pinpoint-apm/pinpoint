import { getLocalStorageValue } from './localStorage';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';

describe('Test localStorage utils', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('Test "getLocalStorageValue"', () => {
    test('Return parsed JSON value from localStorage', () => {
      const key = APP_SETTING_KEYS.LANGUAGE;
      const value = 'ko';
      localStorage.setItem(key, JSON.stringify(value));

      const result = getLocalStorageValue(key);
      expect(result).toBe('ko');
    });

    test('Return parsed object from localStorage', () => {
      const key = APP_SETTING_KEYS.FAVORIITE_APPLICATION_LIST;
      const value = [{ name: 'app1', type: 'node' }];
      localStorage.setItem(key, JSON.stringify(value));

      const result = getLocalStorageValue(key);
      expect(result).toEqual([{ name: 'app1', type: 'node' }]);
    });

    test('Return undefined when key does not exist', () => {
      const result = getLocalStorageValue(APP_SETTING_KEYS.LANGUAGE);
      expect(result).toBeUndefined();
    });

    test('Return null when value is "null" string (parsed as null)', () => {
      const key = APP_SETTING_KEYS.LANGUAGE;
      localStorage.setItem(key, 'null');

      const result = getLocalStorageValue(key);
      // JSON.parse('null') returns null, not undefined
      expect(result).toBeNull();
    });

    test('Return undefined when value is "undefined" string', () => {
      const key = APP_SETTING_KEYS.LANGUAGE;
      localStorage.setItem(key, 'undefined');

      const result = getLocalStorageValue(key);
      expect(result).toBeUndefined();
    });

    test('Return undefined when value is invalid JSON', () => {
      const key = APP_SETTING_KEYS.LANGUAGE;
      localStorage.setItem(key, 'invalid json{');

      const result = getLocalStorageValue(key);
      expect(result).toBeUndefined();
    });

    test('Handle number values', () => {
      const key = APP_SETTING_KEYS.DATE_FORMAT;
      const value = 0;
      localStorage.setItem(key, JSON.stringify(value));

      const result = getLocalStorageValue(key);
      expect(result).toBe(0);
    });

    test('Handle boolean values', () => {
      const key = 'test-boolean';
      const value = true;
      localStorage.setItem(key, JSON.stringify(value));

      const result = getLocalStorageValue(key);
      expect(result).toBe(true);
    });

    test('Handle custom string keys', () => {
      const key = 'custom-key';
      const value = 'custom-value';
      localStorage.setItem(key, JSON.stringify(value));

      const result = getLocalStorageValue(key);
      expect(result).toBe('custom-value');
    });
  });
});
