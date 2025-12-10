import { hasParentWithId } from './dom';

describe('Test dom utils', () => {
  describe('Test "hasParentWithId"', () => {
    test('Return true when element has parent with matching id', () => {
      const parent = document.createElement('div');
      parent.id = 'parent-id';
      const child = document.createElement('div');
      parent.appendChild(child);

      const result = hasParentWithId(child, 'parent-id');
      expect(result).toBe(true);
    });

    test('Return true when element itself has matching id', () => {
      const element = document.createElement('div');
      element.id = 'target-id';

      const result = hasParentWithId(element, 'target-id');
      expect(result).toBe(true);
    });

    test('Return false when no parent has matching id', () => {
      const parent = document.createElement('div');
      parent.id = 'other-id';
      const child = document.createElement('div');
      parent.appendChild(child);

      const result = hasParentWithId(child, 'target-id');
      expect(result).toBe(false);
    });

    test('Return false when element is null', () => {
      const result = hasParentWithId(null, 'target-id');
      expect(result).toBe(false);
    });

    test('Return false when element has no parent and id does not match', () => {
      const element = document.createElement('div');
      element.id = 'other-id';

      const result = hasParentWithId(element, 'target-id');
      expect(result).toBe(false);
    });

    test('Handle nested parent hierarchy', () => {
      const grandParent = document.createElement('div');
      grandParent.id = 'grandparent-id';
      const parent = document.createElement('div');
      parent.id = 'parent-id';
      const child = document.createElement('div');
      grandParent.appendChild(parent);
      parent.appendChild(child);

      const result = hasParentWithId(child, 'grandparent-id');
      expect(result).toBe(true);
    });
  });
});
