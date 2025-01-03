import { mergeFilteredMapNodeData, mergeFilteredMapLinkData } from './merge';
import { newLink, newNode, prevLink, prevNode, resultLink, resultNode } from './mergeMock';

describe('Test merge helper utils', () => {
  describe('Test "mergeFilteredMapNodeData"', () => {
    test('Merge two nodes to one node', () => {
      const result = mergeFilteredMapNodeData(prevNode, newNode);
      expect(result).toEqual(resultNode);
    });
  });
  describe('Test "mergeFilteredMapLinkData"', () => {
    test('Merge two links to one link', () => {
      // const input = 3000;
      const result = mergeFilteredMapLinkData(prevLink, newLink);
      expect(result).toMatchObject(resultLink);
    });
  });
});
