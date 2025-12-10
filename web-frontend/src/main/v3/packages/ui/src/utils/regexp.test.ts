import { isRegexString } from './regexp';

describe('Test regexp utils', () => {
  describe('Test "isRegexString"', () => {
    test('Return true for valid regex string with flags', () => {
      expect(isRegexString('/test/gi')).toBe(true);
      expect(isRegexString('/pattern/g')).toBe(true);
      expect(isRegexString('/regex/i')).toBe(true);
      expect(isRegexString('/test/')).toBe(true);
    });

    test('Return true for valid regex string without flags', () => {
      expect(isRegexString('/test/')).toBe(true);
      expect(isRegexString('/pattern/')).toBe(true);
      expect(isRegexString('/^test$/')).toBe(true);
    });

    test('Return false for invalid regex strings', () => {
      expect(isRegexString('test')).toBe(false);
      expect(isRegexString('/test')).toBe(false);
      expect(isRegexString('test/')).toBe(false);
      expect(isRegexString('test/gi')).toBe(false);
      expect(isRegexString('')).toBe(false);
    });

    test('Return false for non-regex strings', () => {
      expect(isRegexString('hello world')).toBe(false);
      expect(isRegexString('123')).toBe(false);
      expect(isRegexString('/test/xyz')).toBe(false); // invalid flag
    });

    test('Handle complex regex patterns', () => {
      expect(isRegexString('/^[a-z]+$/i')).toBe(true);
      expect(isRegexString('/\\d+/g')).toBe(true);
      expect(isRegexString('/test.*pattern/gi')).toBe(true);
    });
  });
});
