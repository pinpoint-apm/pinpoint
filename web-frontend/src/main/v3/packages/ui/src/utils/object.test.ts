import {
  getSortedKeys,
  getDifferentKeys,
  isSameObjectKeys,
  isEmpty,
  getMergedKeys,
} from './object';

describe('Test obejct utils', () => {
  describe('Test "getSortedKeys"', () => {
    test('Convert empty object, undefined or null to empty array', () => {
      const input = {};
      const result = getSortedKeys(input);
      expect(result).toEqual([]);

      const input2 = undefined;
      const result2 = getSortedKeys(input2);
      expect(result2).toEqual([]);

      const input3 = null;
      const result3 = getSortedKeys(input3);
      expect(result3).toEqual([]);
    });
  });

  describe('Test "getDifferentKeys"', () => {
    test('Compare two object keys', () => {
      const a = { a: 1, b: 2, c: 3 };
      const b = { b: 1, c: 2, d: 3 };
      const result = getDifferentKeys(a, b);

      expect(result).toEqual(['d']);

      const a2 = {};
      const b2 = { b: 1, c: 2, d: 3 };
      const result2 = getDifferentKeys(a2, b2);

      expect(result2).toEqual(['b', 'c', 'd']);
    });
  });

  describe('Test "isSameObjectKeys"', () => {
    test('Check if two objects have the same keys', () => {
      const a = { a: 1, b: 2, c: 3 };
      const b = { b: 1, c: 2, d: 3 };
      const result = isSameObjectKeys(a, b);
      expect(result).toEqual(false);

      const a2 = {};
      const b2 = {};
      const result2 = isSameObjectKeys(a2, b2);
      expect(result2).toEqual(true);

      const a3 = undefined;
      const b3 = null;
      const result3 = isSameObjectKeys(a3, b3);
      expect(result3).toEqual(false);

      const a4 = undefined;
      const b4 = {};
      const result4 = isSameObjectKeys(a4, b4);
      expect(result4).toEqual(false);
    });
  });

  describe('Test "isEmpty"', () => {
    test('Check if input is Empty', () => {
      const input = undefined;
      const result = isEmpty(input);
      expect(result).toEqual(true);

      const input2 = null;
      const resul2t = isEmpty(input2);
      expect(resul2t).toEqual(true);
    });

    test('Check if input is array', () => {
      const input = [] as unknown[];
      const result = isEmpty(input);
      expect(result).toEqual(true);
    });

    test('Check if input is object', () => {
      const input = {};
      const result = isEmpty(input);
      expect(result).toEqual(true);
    });
  });

  describe('Test "getMergedKeys"', () => {
    test('Merge keys from multiple objects into a single array', () => {
      const input = [
        { a: 1, b: 1 },
        { c: 1, d: 1 },
      ];
      const result = getMergedKeys(...input);
      expect(result).toEqual(['a', 'b', 'c', 'd']);
    });
  });
});
