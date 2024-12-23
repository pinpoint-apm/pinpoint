import { addCommas, abbreviateNumber, numberInDecimal, numberInInteger } from './number';

describe('Test number utils', () => {
  describe('Test "addCommas"', () => {
    test('Add Commas to a Number or String Every Three Digits', () => {
      const input = '123456';
      const result = addCommas(input);
      expect(result).toEqual('123,456');

      const input2 = 123456;
      const result2 = addCommas(input2);
      expect(result2).toEqual('123,456');
    });
  });

  describe('Test "addCommas"', () => {
    test('Abbreviate Numbers with Units Array (e.g., ["ms", "sec"]', () => {
      const input = 3000;
      const units = ['ms', 'sec'];
      const result = abbreviateNumber(input, units);
      expect(result).toEqual('3sec');

      const input2 = 1234567;
      const units2 = ['', 'K', 'M', 'G'];
      const result2 = abbreviateNumber(input2, units2);
      expect(result2).toEqual('1.23M');
    });
  });

  describe('Test "numberInDecimal"', () => {
    test('Convert Input to Decimal Format', () => {
      const input = 123456;
      const result = numberInDecimal(input, 2);
      expect(result).toEqual('123456.00');

      const input2 = 123.456;
      const result2 = numberInDecimal(input2, 1);
      expect(result2).toEqual('123.5');
    });
  });

  describe('Test "numberInInteger"', () => {
    test('Convert Input to an Integer', () => {
      const input = 1234.56;
      const result = numberInInteger(input);
      expect(result).toEqual(1235);
    });
  });
});
