import { subDays } from 'date-fns';
import {
  isValidDateRange,
  convertToTimeUnit,
  getParsedDateRange,
  getFormattedDateRange,
  getParsedDate,
  getParsedDates,
  spilitDateStringByHour,
  formatNewLinedDateString,
  convertTimeStringToTime,
  isValidTimezone,
} from './date';

// Mock format utils
jest.mock('./format', () => ({
  format: jest.fn((date, formatStr) => {
    if (formatStr) return formatStr;
    return date.toISOString();
  }),
  getCurrentFormat: jest.fn(() => 'yyyy.MM.dd HH:mm:ss'),
  getTimezone: jest.fn(() => 'Asia/Seoul'),
}));

describe('Test date utils', () => {
  describe('Test "isValidDateRange"', () => {
    test('Two Date Ranges are Greater Than a Given Value', () => {
      const to = new Date(1699595878754);
      const from = subDays(to, 3);
      const result = isValidDateRange(2)({ from, to });
      expect(result).toEqual(false);

      const to2 = new Date(1699595878754);
      const from2 = subDays(to, 1);
      const result2 = isValidDateRange(2)({ from: from2, to: to2 });
      expect(result2).toEqual(true);
    });

    test('Return false when from date is invalid', () => {
      const from = new Date('invalid');
      const to = new Date();
      const result = isValidDateRange(2)({ from, to });
      expect(result).toBe(false);
    });

    test('Return false when to date is invalid', () => {
      const from = new Date();
      const to = new Date('invalid');
      const result = isValidDateRange(2)({ from, to });
      expect(result).toBe(false);
    });

    test('Return false when from is after to', () => {
      const from = new Date();
      const to = subDays(from, 1);
      const result = isValidDateRange(2)({ from, to });
      expect(result).toBe(false);
    });
  });

  describe('Test "convertToTimeUnit"', () => {
    test('Abbreviate Numbers with Units Array (e.g., ["ms", "sec"]', () => {
      const input = 3000;
      const result = convertToTimeUnit(input);
      expect(result).toEqual('3s');

      const input2 = 3000 * 60;
      const result2 = convertToTimeUnit(input2);
      expect(result2).toEqual('3m');

      const input3 = 3000 * 60 * 60;
      const result3 = convertToTimeUnit(input3);
      expect(result3).toEqual('3h');

      const input4 = 3000 * 60 * 60 * 24;
      const result4 = convertToTimeUnit(input4);
      expect(result4).toEqual('3d');
    });

    test('Handle zero milliseconds', () => {
      const result = convertToTimeUnit(0);
      expect(result).toEqual('0s');
    });

    test('Handle days >= 2', () => {
      const result = convertToTimeUnit(2 * 24 * 60 * 60 * 1000);
      expect(result).toEqual('2d');
    });

    test('Handle exactly 1 day', () => {
      const result = convertToTimeUnit(1 * 24 * 60 * 60 * 1000);
      expect(result).toEqual('1d');
    });
  });

  describe('Test "getParsedDateRange"', () => {
    test('Return default date range when no dates provided', () => {
      const result = getParsedDateRange();
      expect(result.from).toBeInstanceOf(Date);
      expect(result.to).toBeInstanceOf(Date);
      expect(result.from.getTime()).toBeLessThan(result.to.getTime());
    });

    test('Parse string dates', () => {
      const from = '2023-11-10-11-29-49';
      const to = '2023-11-10-11-34-49';
      const result = getParsedDateRange({ from, to });
      expect(result.from).toBeInstanceOf(Date);
      expect(result.to).toBeInstanceOf(Date);
    });

    test('Use Date objects directly', () => {
      const from = new Date('2023-11-10T11:29:49');
      const to = new Date('2023-11-10T11:34:49');
      const result = getParsedDateRange({ from, to });
      expect(result.from).toEqual(from);
      expect(result.to).toEqual(to);
    });

    test('Use default range when validation fails', () => {
      const from = new Date('2023-11-10T11:29:49');
      const to = subDays(from, 10); // Invalid: from is after to
      const result = getParsedDateRange({ from, to }, isValidDateRange(2));
      expect(result.from).toBeInstanceOf(Date);
      expect(result.to).toBeInstanceOf(Date);
    });
  });

  describe('Test "getFormattedDateRange"', () => {
    test('Format date range with default format', () => {
      const dateRange = {
        from: new Date('2023-11-10T11:29:49'),
        to: new Date('2023-11-10T11:34:49'),
      };
      const result = getFormattedDateRange(dateRange);
      expect(result.from).toBeTruthy();
      expect(result.to).toBeTruthy();
    });

    test('Format date range with custom format', () => {
      const dateRange = {
        from: new Date('2023-11-10T11:29:49'),
        to: new Date('2023-11-10T11:34:49'),
      };
      const customFormat = 'yyyy-MM-dd';
      const result = getFormattedDateRange(dateRange, customFormat);
      expect(result.from).toBe(customFormat);
      expect(result.to).toBe(customFormat);
    });
  });

  describe('Test "getParsedDate"', () => {
    test('Parse valid date string', () => {
      const dateString = '2023-11-10-11-29-49';
      const result = getParsedDate(dateString);
      expect(result).toBeInstanceOf(Date);
      expect(isNaN(result.getTime())).toBe(false);
    });

    test('Return current date for invalid date string', () => {
      const dateString = 'invalid-date';
      const before = new Date();
      const result = getParsedDate(dateString);
      const after = new Date();
      expect(result.getTime()).toBeGreaterThanOrEqual(before.getTime());
      expect(result.getTime()).toBeLessThanOrEqual(after.getTime());
    });
  });

  describe('Test "getParsedDates"', () => {
    test('Parse two date strings to timestamps', () => {
      const from = '2023-11-10-11-29-49';
      const to = '2023-11-10-11-34-49';
      const result = getParsedDates(from, to);
      expect(result).toHaveLength(2);
      expect(typeof result[0]).toBe('number');
      expect(typeof result[1]).toBe('number');
      expect(result[0]).toBeLessThan(result[1]);
    });
  });

  describe('Test "spilitDateStringByHour"', () => {
    test('Split date string by hour delimiter', () => {
      const dateString = 'yyyy.MM.dd hh:mm:ss aa';
      const result = spilitDateStringByHour(dateString);
      expect(result).toHaveLength(2);
      expect(result[0]).toBeTruthy();
      expect(result[1]).toBeTruthy();
    });

    test('Handle date string without hour delimiter', () => {
      const dateString = 'yyyy.MM.dd HH:mm:ss';
      const result = spilitDateStringByHour(dateString);
      expect(result).toHaveLength(2);
    });
  });

  describe('Test "formatNewLinedDateString"', () => {
    test('Format date with newline separator', () => {
      const date = new Date('2023-11-10T11:29:49');
      const result = formatNewLinedDateString(date);
      expect(result).toContain('\n');
    });

    test('Format timestamp with newline separator', () => {
      const timestamp = new Date('2023-11-10T11:29:49').getTime();
      const result = formatNewLinedDateString(timestamp);
      expect(result).toContain('\n');
    });
  });

  describe('Test "convertTimeStringToTime"', () => {
    test('Convert minutes to milliseconds', () => {
      const result = convertTimeStringToTime('30m');
      expect(result).toBe(30 * 60 * 1000);
    });

    test('Convert hours to milliseconds', () => {
      const result = convertTimeStringToTime('2h');
      expect(result).toBe(2 * 60 * 60 * 1000);
    });

    test('Convert days to milliseconds', () => {
      const result = convertTimeStringToTime('1d');
      expect(result).toBe(1 * 24 * 60 * 60 * 1000);
    });

    test('Throw error for unknown time unit', () => {
      expect(() => convertTimeStringToTime('10x')).toThrow('Unknown time unit');
    });

    test('Handle invalid format', () => {
      expect(() => convertTimeStringToTime('invalid')).toThrow('Unknown time unit');
    });
  });

  describe('Test "isValidTimezone"', () => {
    test('Return true for valid timezone', () => {
      const result = isValidTimezone('Asia/Seoul');
      expect(result).toBe(true);
    });

    test('Return true for valid timezone (America/New_York)', () => {
      const result = isValidTimezone('America/New_York');
      expect(result).toBe(true);
    });

    test('Return false for invalid timezone', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
      const result = isValidTimezone('Invalid/Timezone');
      expect(result).toBe(false);
      consoleSpy.mockRestore();
    });
  });
});
