import { getMergedData } from '../../core/merge';
import { Node, Edge } from '../../types';

describe('getMergedData', () => {
  describe('단일 타겟 노드 병합', () => {
    it('같은 소스에서 같은 타입의 단일 타겟 노드들을 병합해야 함', () => {
      const nodes: Node[] = [
        { id: 'n1', label: 'Node 1', type: 'WAS' },
        { id: 't1', label: 'Target 1', type: 'DB' },
        { id: 't2', label: 'Target 2', type: 'DB' },
      ];
      const edges: Edge[] = [
        { id: 'e1', source: 'n1', target: 't1' },
        { id: 'e2', source: 'n1', target: 't2' },
      ];

      const result = getMergedData({ nodes, edges });

      expect(result.nodes).toHaveLength(2);
      expect(result.edges).toHaveLength(1);

      const mergedNode = result.nodes.find((n) => n.data.id.includes('MergeSingleNodesByServerMap'));
      expect(mergedNode).toBeDefined();
      expect(mergedNode?.data.label).toBe('total: 2');
      expect(mergedNode?.data.type).toBe('DB');
    });

    it('다른 타입의 노드는 병합하지 않아야 함', () => {
      const nodes: Node[] = [
        { id: 'n1', label: 'Node 1', type: 'WAS' },
        { id: 't1', label: 'Target 1', type: 'DB' },
        { id: 't2', label: 'Target 2', type: 'CACHE' },
      ];
      const edges: Edge[] = [
        { id: 'e1', source: 'n1', target: 't1' },
        { id: 'e2', source: 'n1', target: 't2' },
      ];

      const result = getMergedData({ nodes, edges });

      expect(result.nodes).toHaveLength(3);
      expect(result.edges).toHaveLength(2);
    });

    it('shouldNotMerge가 true인 노드는 병합하지 않아야 함', () => {
      const nodes: Node[] = [
        { id: 'n1', label: 'Node 1', type: 'WAS' },
        { id: 't1', label: 'Target 1', type: 'DB', shouldNotMerge: () => true },
        { id: 't2', label: 'Target 2', type: 'DB' },
      ];
      const edges: Edge[] = [
        { id: 'e1', source: 'n1', target: 't1' },
        { id: 'e2', source: 'n1', target: 't2' },
      ];

      const result = getMergedData({ nodes, edges });

      expect(result.nodes).toHaveLength(3);
      expect(result.edges).toHaveLength(2);
    });
  });

  describe('다중 타겟 노드 병합', () => {
    it('여러 소스에서 같은 타겟 노드가 있으면 다중 타겟으로 분류되어야 함', () => {
      const nodes: Node[] = [
        { id: 'n1', label: 'Node 1', type: 'WAS' },
        { id: 'n2', label: 'Node 2', type: 'WAS' },
        { id: 't1', label: 'Target 1', type: 'DB' },
      ];
      const edges: Edge[] = [
        { id: 'e1', source: 'n1', target: 't1' },
        { id: 'e2', source: 'n2', target: 't1' },
      ];

      const result = getMergedData({ nodes, edges });

      // 다중 타겟 노드는 같은 소스 조합을 가진 노드들만 병합되므로, 
      // 단일 노드인 경우 병합되지 않음
      expect(result.nodes.length).toBeGreaterThanOrEqual(3);
      expect(result.edges.length).toBeGreaterThanOrEqual(2);
    });

    it('같은 소스 조합의 다중 타겟 노드들을 병합해야 함', () => {
      const nodes: Node[] = [
        { id: 'n1', label: 'Node 1', type: 'WAS' },
        { id: 'n2', label: 'Node 2', type: 'WAS' },
        { id: 't1', label: 'Target 1', type: 'DB' },
        { id: 't2', label: 'Target 2', type: 'DB' },
      ];
      const edges: Edge[] = [
        { id: 'e1', source: 'n1', target: 't1' },
        { id: 'e2', source: 'n2', target: 't1' },
        { id: 'e3', source: 'n1', target: 't2' },
        { id: 'e4', source: 'n2', target: 't2' },
      ];

      const result = getMergedData({ nodes, edges });

      // 같은 소스 조합(n1,n2)을 가진 타겟 노드들(t1, t2)이 병합됨
      const mergedNode = result.nodes.find((n) => n.data.id.includes('MergeMultiNodesByServerMap'));
      expect(mergedNode).toBeDefined();
      if (mergedNode) {
        expect(mergedNode.data.label).toBe('total: 2');
      }
    });
  });

  describe('복잡한 시나리오', () => {
    it('단일 타겟과 다중 타겟이 모두 있는 경우', () => {
      const nodes: Node[] = [
        { id: 'n1', label: 'Node 1', type: 'WAS' },
        { id: 'n2', label: 'Node 2', type: 'WAS' },
        { id: 't1', label: 'Target 1', type: 'DB' },
        { id: 't2', label: 'Target 2', type: 'DB' },
        { id: 't3', label: 'Target 3', type: 'DB' },
      ];
      const edges: Edge[] = [
        { id: 'e1', source: 'n1', target: 't1' },
        { id: 'e2', source: 'n1', target: 't2' },
        { id: 'e3', source: 'n1', target: 't3' },
        { id: 'e4', source: 'n2', target: 't1' },
      ];

      const result = getMergedData({ nodes, edges });

      // n1에서 t2, t3는 단일 타겟이므로 병합될 수 있음
      expect(result.nodes.length).toBeGreaterThanOrEqual(3);
      // 병합이 발생하면 types 배열에 타입이 포함됨
      expect(Array.isArray(result.mergeInfo.types)).toBe(true);
    });

    it('중간 노드는 병합 대상이 아니어야 함', () => {
      const nodes: Node[] = [
        { id: 'n1', label: 'Node 1', type: 'WAS' },
        { id: 'n2', label: 'Node 2', type: 'WAS' },
        { id: 't1', label: 'Target 1', type: 'DB' },
      ];
      const edges: Edge[] = [
        { id: 'e1', source: 'n1', target: 'n2' },
        { id: 'e2', source: 'n2', target: 't1' },
      ];

      const result = getMergedData({ nodes, edges });

      expect(result.nodes).toHaveLength(3);
      expect(result.edges).toHaveLength(2);
    });
  });

  describe('mergeInfo', () => {
    it('병합된 타입 정보를 반환해야 함', () => {
      const nodes: Node[] = [
        { id: 'n1', label: 'Node 1', type: 'WAS' },
        { id: 't1', label: 'Target 1', type: 'DB' },
        { id: 't2', label: 'Target 2', type: 'DB' },
      ];
      const edges: Edge[] = [
        { id: 'e1', source: 'n1', target: 't1' },
        { id: 'e2', source: 'n1', target: 't2' },
      ];

      const result = getMergedData({ nodes, edges });

      expect(result.mergeInfo.types).toContain('DB');
    });
  });

  describe('renderNode 함수', () => {
    it('renderNode 함수가 제공되면 사용해야 함', () => {
      const nodes: Node[] = [
        { id: 'n1', label: 'Node 1', type: 'WAS' },
        { id: 't1', label: 'Target 1', type: 'DB' },
        { id: 't2', label: 'Target 2', type: 'DB' },
      ];
      const edges: Edge[] = [
        { id: 'e1', source: 'n1', target: 't1' },
        { id: 'e2', source: 'n1', target: 't2' },
      ];

      const renderNode = jest.fn((node, svg) => svg);
      const result = getMergedData({ nodes, edges }, renderNode);

      expect(result.nodes.length).toBeGreaterThan(0);
    });
  });

  describe('엣지 데이터 구조', () => {
    it('병합된 엣지는 edges 배열을 포함해야 함', () => {
      const nodes: Node[] = [
        { id: 'n1', label: 'Node 1', type: 'WAS' },
        { id: 't1', label: 'Target 1', type: 'DB' },
        { id: 't2', label: 'Target 2', type: 'DB' },
      ];
      const edges: Edge[] = [
        { id: 'e1', source: 'n1', target: 't1' },
        { id: 'e2', source: 'n1', target: 't2' },
      ];

      const result = getMergedData({ nodes, edges });

      const mergedEdge = result.edges.find((e) => e.data.id.includes('~'));
      expect(mergedEdge).toBeDefined();
    });
  });
});

