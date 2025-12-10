import { insertIf } from './array';

describe('Test array utils', () => {
  describe('Test "insertIf"', () => {
    test('Return element array when condition is true', () => {
      const condition = true;
      const element = () => [1, 2, 3];
      const result = insertIf(condition, element);
      expect(result).toEqual([1, 2, 3]);
    });

    test('Return empty array when condition is false', () => {
      const condition = false;
      const element = () => [1, 2, 3];
      const result = insertIf(condition, element);
      expect(result).toEqual([]);
    });

    test('Return element array when condition function returns true', () => {
      const condition = () => true;
      const element = () => ['a', 'b'];
      const result = insertIf(condition, element);
      expect(result).toEqual(['a', 'b']);
    });

    test('Return empty array when condition function returns false', () => {
      const condition = () => false;
      const element = () => ['a', 'b'];
      const result = insertIf(condition, element);
      expect(result).toEqual([]);
    });

    test('Handle complex condition logic', () => {
      const value = 10;
      const condition = value > 5;
      const element = () => [{ id: 1, value }];
      const result = insertIf(condition, element);
      expect(result).toEqual([{ id: 1, value: 10 }]);
    });
  });
});
