import { getMaxTickValue } from './charts';

describe('Test charts helper utils', () => {
  describe('Test "getMaxTickValue"', () => {
    test('Two Date Ranges are Greater Than a Given Value', () => {
      const input = [[108729, 18, 4, 0, 6]];

      const result = getMaxTickValue(input);
      expect(result).toEqual(200000);
    });

    test('Two Date Ranges are Greater Than a Given Value', () => {
      const input = [[169, 0, 0, 0, 0]];

      const result = getMaxTickValue(input);
      expect(result).toEqual(300);
    });
  });
});
