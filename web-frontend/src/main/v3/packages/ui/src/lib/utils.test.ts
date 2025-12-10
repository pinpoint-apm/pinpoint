import { cn } from './utils';

describe('Test lib utils', () => {
  describe('Test "cn"', () => {
    test('Merge class names from multiple arguments', () => {
      const result = cn('class1', 'class2', 'class3');
      expect(result).toBe('class1 class2 class3');
    });

    test('Handle conditional classes', () => {
      const condition = true;
      const result = cn('base', condition && 'conditional');
      expect(result).toBe('base conditional');
    });

    test('Handle false conditional classes', () => {
      const condition = false;
      const result = cn('base', condition && 'conditional');
      expect(result).toBe('base');
    });

    test('Merge Tailwind classes with conflicts', () => {
      const result = cn('px-2 py-1', 'px-4');
      // tailwind-merge should resolve conflict, keeping px-4
      expect(result).toContain('px-4');
      expect(result).toContain('py-1');
    });

    test('Handle undefined and null values', () => {
      const result = cn('base', undefined, null, 'valid');
      expect(result).toBe('base valid');
    });

    test('Handle empty strings', () => {
      const result = cn('base', '', 'valid');
      expect(result).toBe('base valid');
    });

    test('Handle array of classes', () => {
      const result = cn(['class1', 'class2'], 'class3');
      expect(result).toBe('class1 class2 class3');
    });

    test('Handle object with conditional classes', () => {
      const result = cn({
        class1: true,
        class2: false,
        class3: true,
      });
      expect(result).toBe('class1 class3');
    });

    test('Handle mixed arguments', () => {
      const result = cn('base', ['array1', 'array2'], { obj1: true, obj2: false }, 'final');
      expect(result).toContain('base');
      expect(result).toContain('array1');
      expect(result).toContain('array2');
      expect(result).toContain('obj1');
      expect(result).not.toContain('obj2');
      expect(result).toContain('final');
    });
  });
});
