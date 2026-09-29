import cytoscape from 'cytoscape';
import {
  layoutGroupChildren,
  placeAnchoredNodes,
  ShiftRecord,
  snapshotNodes,
} from '../../core/anchor';

const createCy = () =>
  cytoscape({
    headless: true,
    styleEnabled: true,
    style: [
      { selector: 'node', style: { width: 100, height: 100 } },
      { selector: 'node:parent', style: { padding: '30px' } },
    ],
  });

const positionOf = (cy: cytoscape.Core, id: string) => ({ ...cy.getElementById(id).position() });

describe('layoutGroupChildren', () => {
  it('stacks unconnected children in one column centered at the origin', () => {
    const positions = layoutGroupChildren(['a', 'b', 'c'], []);

    expect(positions.a.x).toBe(0);
    expect(positions.b).toEqual({ x: 0, y: 0 });
    expect(positions.a.y).toBe(-positions.c.y);
    expect(positions.a.y).toBeLessThan(0);
  });

  it('puts callees to the right of their callers', () => {
    const positions = layoutGroupChildren(
      ['a', 'b', 'c'],
      [
        ['a', 'b'],
        ['b', 'c'],
      ],
    );

    expect(positions.a.x).toBeLessThan(positions.b.x);
    expect(positions.b.x).toBeLessThan(positions.c.x);
    expect(positions.b.x).toBe(0);
  });

  it('keeps up to 8 children in one column', () => {
    const ids = Array.from({ length: 8 }, (_, i) => `n${i}`);
    const positions = layoutGroupChildren(ids, []);

    expect(new Set(ids.map((id) => positions[id].x))).toEqual(new Set([0]));
  });

  it('wraps many children into a grid close to a square', () => {
    const ids = Array.from({ length: 50 }, (_, i) => `n${i}`);
    const positions = layoutGroupChildren(ids, []);
    const xs = ids.map((id) => positions[id].x);
    const ys = ids.map((id) => positions[id].y);
    const width = Math.max(...xs) - Math.min(...xs);
    const height = Math.max(...ys) - Math.min(...ys);

    expect(new Set(xs).size).toBeGreaterThan(1);
    expect(width / height).toBeGreaterThan(0.5);
    expect(width / height).toBeLessThan(2);
    // 가운데가 원점이다.
    expect(Math.min(...xs)).toBe(-Math.max(...xs));
    // 겹치는 노드가 없다.
    expect(new Set(ids.map((id) => `${positions[id].x},${positions[id].y}`)).size).toBe(50);
  });

  it('keeps a caller and its callee side by side on the same row when wrapping', () => {
    // c0 → c1, c2 → c3, … 25쌍. 호출자는 왼쪽 열, 피호출자는 오른쪽 열이 된다.
    const ids = Array.from({ length: 50 }, (_, i) => `c${i}`);
    const edges = Array.from(
      { length: 25 },
      (_, i) => [`c${i * 2}`, `c${i * 2 + 1}`] as [string, string],
    );
    const positions = layoutGroupChildren(ids, edges);

    edges.forEach(([caller, callee]) => {
      expect(positions[callee].y).toBe(positions[caller].y);
      expect(positions[callee].x).toBeGreaterThan(positions[caller].x);
    });
    expect(new Set(ids.map((id) => positions[id].x)).size).toBeGreaterThan(2);
  });

  it('ignores self loops, outside edges and ends on cycles', () => {
    const positions = layoutGroupChildren(
      ['a', 'b'],
      [
        ['a', 'a'],
        ['a', 'outside'],
        ['a', 'b'],
        ['b', 'a'],
      ],
    );

    expect(Object.keys(positions).sort()).toEqual(['a', 'b']);
    expect(Number.isFinite(positions.a.x)).toBe(true);
    expect(Number.isFinite(positions.b.x)).toBe(true);
  });
});

describe('placeAnchoredNodes', () => {
  // left(-400) → group(0) → right(400), group 바로 아래의 below(0, 300), 그리고 대각선의 far
  const setup = () => {
    const cy = createCy();
    cy.add([
      { data: { id: 'left' }, position: { x: -400, y: 0 } },
      { data: { id: 'group' }, position: { x: 0, y: 0 } },
      { data: { id: 'right' }, position: { x: 400, y: 0 } },
      { data: { id: 'below' }, position: { x: 0, y: 300 } },
      { data: { id: 'far' }, position: { x: -400, y: 400 } },
    ]);
    return cy;
  };

  const expand = (cy: cytoscape.Core, shifts: Map<string, ShiftRecord>) => {
    const snapshot = snapshotNodes(cy);
    cy.getElementById('group').remove();
    const added = [
      cy.add({ data: { id: 'box', anchorId: 'group' } }),
      cy.add({ data: { id: 'c1', parent: 'box', anchorId: 'group' } }),
      cy.add({ data: { id: 'c2', parent: 'box', anchorId: 'group' } }),
    ];
    placeAnchoredNodes(cy, added, snapshot, shifts);
  };

  const collapse = (cy: cytoscape.Core, shifts: Map<string, ShiftRecord>) => {
    const snapshot = snapshotNodes(cy);
    cy.getElementById('box').remove();
    const added = [cy.add({ data: { id: 'group', anchorId: 'box' } })];
    placeAnchoredNodes(cy, added, snapshot, shifts);
  };

  it('places the children around where the group node was', () => {
    const cy = setup();
    expand(cy, new Map());

    const box = cy.getElementById('box');
    expect(box.position().x).toBeCloseTo(0);
    expect(box.position().y).toBeCloseTo(0);
    expect(positionOf(cy, 'c1').x).toBe(positionOf(cy, 'c2').x);
  });

  it('moves the neighbors out of the grown box without changing their order', () => {
    const cy = setup();
    expand(cy, new Map());

    const box = cy.getElementById('box').boundingBox();
    const left = cy.getElementById('left').boundingBox();
    const right = cy.getElementById('right').boundingBox();
    const below = cy.getElementById('below').boundingBox();

    expect(left.x2).toBeLessThanOrEqual(box.x1);
    expect(right.x1).toBeGreaterThanOrEqual(box.x2);
    expect(below.y1).toBeGreaterThanOrEqual(box.y2);
    // 옆에 있는 노드는 옆으로만, 아래에 있는 노드는 아래로만 민다.
    expect(positionOf(cy, 'left').y).toBe(0);
    expect(positionOf(cy, 'below').x).toBe(0);
  });

  it('leaves nodes that are not in the way where they are', () => {
    const cy = setup();
    expand(cy, new Map());

    expect(positionOf(cy, 'far')).toEqual({ x: -400, y: 400 });
  });

  it('puts the group back and restores the neighbors when collapsed', () => {
    const cy = setup();
    const shifts = new Map<string, ShiftRecord>();
    expand(cy, shifts);
    collapse(cy, shifts);

    expect(positionOf(cy, 'group')).toEqual({ x: 0, y: 0 });
    expect(positionOf(cy, 'left')).toEqual({ x: -400, y: 0 });
    expect(positionOf(cy, 'right')).toEqual({ x: 400, y: 0 });
    expect(positionOf(cy, 'below')).toEqual({ x: 0, y: 300 });
    expect(shifts.size).toBe(0);
  });

  it('collapses the group where the box was moved to', () => {
    const cy = setup();
    const shifts = new Map<string, ShiftRecord>();
    expand(cy, shifts);
    cy.getElementById('box').children().shift({ x: 50, y: 20 });
    collapse(cy, shifts);

    expect(positionOf(cy, 'group').x).toBeCloseTo(50);
    expect(positionOf(cy, 'group').y).toBeCloseTo(20);
  });
});
