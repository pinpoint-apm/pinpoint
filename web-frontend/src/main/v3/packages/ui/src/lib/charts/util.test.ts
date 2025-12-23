import { toCssVariable } from './util';

describe('Test charts util', () => {
  describe('toCssVariable', () => {
    it('should convert string with special characters to CSS variable format', () => {
      const input = 'test:value;tag';
      const result = toCssVariable(input);

      expect(result).not.toContain(':');
      expect(result).not.toContain(';');
      expect(result).toContain('_');
    });

    it('should handle string with colon', () => {
      const input = 'test:value';
      const result = toCssVariable(input);

      expect(result).toBe('test_3avalue');
    });

    it('should handle string with semicolon', () => {
      const input = 'test;value';
      const result = toCssVariable(input);

      expect(result).toBe('test_3bvalue');
    });

    it('should handle alphanumeric string without special characters', () => {
      const input = 'testValue123';
      const result = toCssVariable(input);

      expect(result).toBe('testValue123');
    });

    it('should handle empty string', () => {
      const input = '';
      const result = toCssVariable(input);

      expect(result).toBe('');
    });

    it('should handle string with multiple special characters', () => {
      const input = 'test:value;tag@name';
      const result = toCssVariable(input);

      expect(result).not.toContain(':');
      expect(result).not.toContain(';');
      expect(result).not.toContain('@');
    });

    it('should handle string with spaces', () => {
      const input = 'test value';
      const result = toCssVariable(input);

      expect(result).toBe('test_20value');
    });

    it('should handle string with hyphens', () => {
      const input = 'test-value';
      const result = toCssVariable(input);

      expect(result).toBe('test_2dvalue');
    });

    it('should convert special characters to hex code with underscore prefix', () => {
      const input = ':';
      const result = toCssVariable(input);

      // Colon (:) has char code 58, which is 3a in hex
      expect(result).toBe('_3a');
    });
  });
});
