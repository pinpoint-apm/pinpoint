import { subDays } from 'date-fns';
import { isValidDateRange, convertToTimeUnit } from './date';

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
  });
});
