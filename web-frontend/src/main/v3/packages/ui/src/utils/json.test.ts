import { safeParse } from './json';

describe('Test json utils', () => {
  describe('Test "safeParse"', () => {
    test('Return parsed value for valid JSON', () => {
      const result = safeParse(JSON.stringify({ a: 1 }));
      expect(result).toEqual({ a: 1 });

      const result2 = safeParse(JSON.stringify('text'));
      expect(result2).toBe('text');
    });

    test('Return undefined for null or undefined', () => {
      const result = safeParse(null);
      expect(result).toBeUndefined();

      // @ts-expect-error Testing invalid argument intentionally
      const result2 = safeParse(undefined);
      expect(result2).toBeUndefined();
    });

    test('Return undefined for "undefined" string', () => {
      const result = safeParse('undefined');
      expect(result).toBeUndefined();
    });

    test('Return null for "null" string', () => {
      const result = safeParse('null');
      expect(result).toBeNull();
    });

    test('Return undefined for invalid JSON', () => {
      const result = safeParse('invalid json{');
      expect(result).toBeUndefined();
    });

    test('Return undefined for empty string', () => {
      const result = safeParse('');
      expect(result).toBeUndefined();
    });
  });
});
