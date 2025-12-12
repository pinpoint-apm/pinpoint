import { format, getTimezone, getCurrentFormat } from './format';
import { APP_SETTING_KEYS, DATE_FORMATS } from '@pinpoint-fe/ui/src/constants';
import { getLocalStorageValue } from './localStorage';
import { formatInTimeZone } from 'date-fns-tz';

// Mock localStorage and date utils
jest.mock('./localStorage', () => ({
  getLocalStorageValue: jest.fn(),
}));

jest.mock('./date', () => ({
  isValidTimezone: jest.fn(),
}));

jest.mock('date-fns-tz', () => ({
  formatInTimeZone: jest.fn((date, timezone, formatStr) => {
    // Simple mock that returns formatted string
    if (formatStr === 'yyyy-MM-dd') {
      return '2023-11-10';
    }
    return '2023.11.10 12:00:00';
  }),
}));

import { isValidTimezone } from './date';

describe('Test format utils', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    // Mock Intl.DateTimeFormat for timezone tests
    jest.spyOn(Intl, 'DateTimeFormat').mockImplementation(() => {
      return {
        resolvedOptions: () => ({ timeZone: 'Asia/Seoul' }),
      } as any;
    });
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('Test "getTimezone"', () => {
    test('Return timezone from localStorage when valid', () => {
      (getLocalStorageValue as jest.Mock).mockReturnValue('America/New_York');
      (isValidTimezone as jest.Mock).mockReturnValue(true);
      const result = getTimezone();
      expect(result).toBe('America/New_York');
    });

    test('Return system timezone when localStorage value is invalid', () => {
      (getLocalStorageValue as jest.Mock).mockReturnValue('Invalid/Timezone');
      (isValidTimezone as jest.Mock).mockReturnValue(false);
      const result = getTimezone();
      expect(result).toBe('Asia/Seoul');
    });

    test('Return system timezone when localStorage value is undefined', () => {
      (getLocalStorageValue as jest.Mock).mockReturnValue(undefined);
      (isValidTimezone as jest.Mock).mockReturnValue(false);
      const result = getTimezone();
      expect(result).toBe('Asia/Seoul');
    });
  });

  describe('Test "getCurrentFormat"', () => {
    test('Return format from localStorage when valid index', () => {
      (getLocalStorageValue as jest.Mock).mockReturnValue(0);
      const result = getCurrentFormat();
      expect(result).toBe(DATE_FORMATS[0]);
    });

    test('Return default format (index 0) when index is out of range', () => {
      (getLocalStorageValue as jest.Mock).mockReturnValue(100);
      const result = getCurrentFormat();
      expect(result).toBe(DATE_FORMATS[0]);
    });

    test('Return default format (index 0) when index is negative', () => {
      (getLocalStorageValue as jest.Mock).mockReturnValue(-1);
      const result = getCurrentFormat();
      expect(result).toBe(DATE_FORMATS[0]);
    });

    test('Return default format (index 0) when value is undefined', () => {
      (getLocalStorageValue as jest.Mock).mockReturnValue(undefined);
      const result = getCurrentFormat();
      expect(result).toBe(DATE_FORMATS[0]);
    });
  });

  describe('Test "format"', () => {
    beforeEach(() => {
      (getLocalStorageValue as jest.Mock).mockImplementation((key: string) => {
        if (key === APP_SETTING_KEYS.TIMEZONE) return 'UTC';
        if (key === APP_SETTING_KEYS.DATE_FORMAT) return 0;
        if (key === APP_SETTING_KEYS.LANGUAGE) return 'en';
        return undefined;
      });
      (isValidTimezone as jest.Mock).mockReturnValue(true);
    });

    test('Format valid date with default format', () => {
      const date = new Date('2023-11-10T12:00:00');
      const result = format(date);
      expect(result).toBeTruthy();
      expect(result).not.toBe('N/A');
      expect(typeof result).toBe('string');
      expect(result.length).toBeGreaterThan(0);
    });

    test('Format valid date with custom format', () => {
      const date = new Date('2023-11-10T12:00:00');
      const result = format(date, 'yyyy-MM-dd');
      expect(result).toBeTruthy();
      expect(result).not.toBe('N/A');
      expect(typeof result).toBe('string');
      expect(result).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    });

    test('Return "N/A" for invalid date', () => {
      const invalidDate = new Date('invalid');
      const result = format(invalidDate);
      expect(result).toBe('N/A');
    });

    test('Format valid timestamp', () => {
      const timestamp = new Date('2023-11-10T12:00:00').getTime();
      const result = format(timestamp);
      expect(result).toBeTruthy();
      expect(result).not.toBe('N/A');
      expect(typeof result).toBe('string');
      expect(result.length).toBeGreaterThan(0);
    });

    test('Return "N/A" for invalid timestamp', () => {
      const invalidTimestamp = NaN;
      const result = format(invalidTimestamp);
      expect(result).toBe('N/A');
    });

    test('Return "N/A" for negative timestamp', () => {
      const negativeTimestamp = -1;
      const result = format(negativeTimestamp);
      expect(result).toBe('N/A');
    });

    test('Handle format with options', () => {
      const date = new Date('2023-11-10T12:00:00');
      const result = format(date, 'yyyy-MM-dd', {});
      expect(result).toBeTruthy();
      expect(result).not.toBe('N/A');
      expect(typeof result).toBe('string');
      expect(result).toMatch(/^\d{4}-\d{2}-\d{2}$/);
    });
  });
});
