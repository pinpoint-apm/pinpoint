import cytoscape from 'cytoscape';
import {
  layoutGroupChildren,
  placeGroupChanges,
  releaseRemovedGroups,
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

describe('placeGroupChanges: expand and collapse', () => {
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
    placeGroupChanges(cy, added, snapshot, shifts);
  };

  const collapse = (cy: cytoscape.Core, shifts: Map<string, ShiftRecord>) => {
    const snapshot = snapshotNodes(cy);
    cy.getElementById('box').remove();
    const added = [cy.add({ data: { id: 'group', anchorId: 'box' } })];
    placeGroupChanges(cy, added, snapshot, shifts);
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
  describe('with two groups in the same row', () => {
    // g1(0) 과 g2(400) 이 같은 행에 있다. 한쪽을 펼치면 다른 쪽이 밀린다.
    const setupTwo = () => {
      const cy = createCy();
      cy.add([
        { data: { id: 'g1' }, position: { x: 0, y: 0 } },
        { data: { id: 'g2' }, position: { x: 400, y: 0 } },
      ]);
      return cy;
    };

    const expandGroup = (cy: cytoscape.Core, shifts: Map<string, ShiftRecord>, id: string) => {
      const snapshot = snapshotNodes(cy);
      cy.getElementById(id).remove();
      const added = [
        cy.add({ data: { id: `${id}-box`, anchorId: id } }),
        cy.add({ data: { id: `${id}-c1`, parent: `${id}-box`, anchorId: id } }),
        cy.add({ data: { id: `${id}-c2`, parent: `${id}-box`, anchorId: id } }),
      ];
      placeGroupChanges(cy, added, snapshot, shifts);
    };

    const collapseGroup = (cy: cytoscape.Core, shifts: Map<string, ShiftRecord>, id: string) => {
      const snapshot = snapshotNodes(cy);
      cy.getElementById(`${id}-box`).children().remove();
      cy.getElementById(`${id}-box`).remove();
      const added = [cy.add({ data: { id, anchorId: `${id}-box` } })];
      placeGroupChanges(cy, added, snapshot, shifts);
    };

    it('restores a child that joined a group after another group pushed it', () => {
      const cy = setupTwo();
      const shifts = new Map<string, ShiftRecord>();
      expandGroup(cy, shifts, 'g1');
      expandGroup(cy, shifts, 'g2');

      // 실시간 갱신으로 g1이 밀어 둔 g2에 자식이 늘어난다.
      const snapshot = snapshotNodes(cy);
      cy.add({
        data: { id: 'g2-c3', parent: 'g2-box', anchorId: 'g2' },
        position: { x: 0, y: 0 },
      });
      placeGroupChanges(cy, [], snapshot, shifts);
      const before = positionOf(cy, 'g2-c1');
      const joinedBefore = positionOf(cy, 'g2-c3');

      collapseGroup(cy, shifts, 'g1');

      // g2의 자식들이 함께 같은 양만큼 되돌아온다.
      const dx = positionOf(cy, 'g2-c1').x - before.x;
      expect(dx).toBeLessThan(0);
      expect(positionOf(cy, 'g2-c3').x - joinedBefore.x).toBeCloseTo(dx);
    });

    it.each([
      ['in the order they were expanded', ['g1', 'g2']],
      ['in the reverse order', ['g2', 'g1']],
    ])('restores both groups when collapsed %s', (_, collapseOrder) => {
      const cy = setupTwo();
      const shifts = new Map<string, ShiftRecord>();
      expandGroup(cy, shifts, 'g1');
      expandGroup(cy, shifts, 'g2');
      collapseOrder.forEach((id) => collapseGroup(cy, shifts, id));

      expect(positionOf(cy, 'g1').x).toBeCloseTo(0);
      expect(positionOf(cy, 'g1').y).toBeCloseTo(0);
      expect(positionOf(cy, 'g2').x).toBeCloseTo(400);
      expect(positionOf(cy, 'g2').y).toBeCloseTo(0);
      expect(shifts.size).toBe(0);
    });
  });
});

describe('placeGroupChanges: children of an expanded box change', () => {
  // group(0, 0)을 펼친 상태에서 오른쪽(right)에 노드가 있다. 실시간 갱신으로 자식이 늘어난다.
  const setup = () => {
    const cy = createCy();
    const shifts = new Map<string, ShiftRecord>();
    cy.add([
      { data: { id: 'group' }, position: { x: 0, y: 0 } },
      { data: { id: 'right' }, position: { x: 400, y: 0 } },
    ]);
    const snapshot = snapshotNodes(cy);
    cy.getElementById('group').remove();
    const added = [
      cy.add({ data: { id: 'box', anchorId: 'group' } }),
      ...Array.from({ length: 8 }, (_, i) =>
        cy.add({ data: { id: `c${i}`, parent: 'box', anchorId: 'group' } }),
      ),
    ];
    placeGroupChanges(cy, added, snapshot, shifts);
    return { cy, shifts };
  };

  // 한 칸에 8개까지 쌓이므로 아홉 번째부터 상자가 옆으로 넓어진다.
  const join = (cy: cytoscape.Core, shifts: Map<string, ShiftRecord>, ids: string[]) => {
    const snapshot = snapshotNodes(cy);
    ids.forEach((id) =>
      cy.add({ data: { id, parent: 'box', anchorId: 'group' }, position: { x: 0, y: 0 } }),
    );
    placeGroupChanges(cy, [], snapshot, shifts);
  };

  it('puts the new child into the grid instead of where it was added', () => {
    const { cy, shifts } = setup();
    join(cy, shifts, ['c8', 'c9']);

    const positions = cy
      .getElementById('box')
      .children()
      .map((child) => `${child.position().x},${child.position().y}`);
    expect(new Set(positions).size).toBe(10);
    expect(positionOf(cy, 'c8').x).not.toBe(positionOf(cy, 'c0').x);
  });

  it('keeps the box centered where it was', () => {
    const { cy, shifts } = setup();
    const children = () => cy.getElementById('box').children();
    const center = () => {
      const xs = children().map((child) => child.position().x);
      const ys = children().map((child) => child.position().y);
      return {
        x: (Math.min(...xs) + Math.max(...xs)) / 2,
        y: (Math.min(...ys) + Math.max(...ys)) / 2,
      };
    };
    join(cy, shifts, ['c8', 'c9']);

    expect(center().x).toBeCloseTo(0);
    expect(center().y).toBeCloseTo(0);
  });

  it('moves the neighbors further out of the grown box', () => {
    const { cy, shifts } = setup();
    join(cy, shifts, ['c8', 'c9']);

    const box = cy.getElementById('box').boundingBox();
    expect(cy.getElementById('right').boundingBox().x1).toBeGreaterThanOrEqual(box.x2);
  });

  it('restores the neighbors by the whole amount when collapsed', () => {
    const { cy, shifts } = setup();
    join(cy, shifts, ['c8', 'c9']);

    const snapshot = snapshotNodes(cy);
    cy.getElementById('box').children().remove();
    cy.getElementById('box').remove();
    const added = [cy.add({ data: { id: 'group', anchorId: 'box' } })];
    placeGroupChanges(cy, added, snapshot, shifts);

    expect(positionOf(cy, 'right')).toEqual({ x: 400, y: 0 });
    expect(shifts.size).toBe(0);
  });

  it('collapses back to where the group was after a child left', () => {
    const { cy, shifts } = setup();

    // 실시간 갱신으로 맨 위의 자식이 빠진다.
    const snapshot = snapshotNodes(cy);
    cy.getElementById('c0').remove();
    placeGroupChanges(cy, [], snapshot, shifts);

    const collapseSnapshot = snapshotNodes(cy);
    cy.getElementById('box').children().remove();
    cy.getElementById('box').remove();
    const added = [cy.add({ data: { id: 'group', anchorId: 'box' } })];
    placeGroupChanges(cy, added, collapseSnapshot, shifts);

    expect(positionOf(cy, 'group').x).toBeCloseTo(0);
    expect(positionOf(cy, 'group').y).toBeCloseTo(0);
    expect(positionOf(cy, 'right')).toEqual({ x: 400, y: 0 });
  });

  it('leaves a box alone when its children did not change', () => {
    const { cy, shifts } = setup();
    const before = positionOf(cy, 'c0');
    const record = new Map(shifts.get('group'));

    placeGroupChanges(cy, [], snapshotNodes(cy), shifts);

    expect(positionOf(cy, 'c0')).toEqual(before);
    expect(shifts.get('group')).toEqual(record);
  });
});

describe('releaseRemovedGroups', () => {
  // group(0, 0)을 펼쳐 오른쪽(right)을 밀어 둔 상태.
  const setup = () => {
    const cy = createCy();
    const shifts = new Map<string, ShiftRecord>();
    cy.add([
      { data: { id: 'group' }, position: { x: 0, y: 0 } },
      { data: { id: 'right' }, position: { x: 400, y: 0 } },
    ]);
    const snapshot = snapshotNodes(cy);
    cy.getElementById('group').remove();
    const added = [
      cy.add({ data: { id: 'box', anchorId: 'group' } }),
      cy.add({ data: { id: 'c1', parent: 'box', anchorId: 'group' } }),
      cy.add({ data: { id: 'c2', parent: 'box', anchorId: 'group' } }),
    ];
    placeGroupChanges(cy, added, snapshot, shifts);
    return { cy, shifts };
  };

  it('moves the neighbors back when an expanded group disappears', () => {
    const { cy, shifts } = setup();
    expect(positionOf(cy, 'right').x).toBeGreaterThan(400);

    cy.getElementById('box').children().remove();
    cy.getElementById('box').remove();
    releaseRemovedGroups(cy, shifts);

    expect(positionOf(cy, 'right')).toEqual({ x: 400, y: 0 });
    expect(shifts.size).toBe(0);
  });

  it('keeps the record of a group that is still drawn', () => {
    const { cy, shifts } = setup();
    const before = positionOf(cy, 'right');

    releaseRemovedGroups(cy, shifts);

    expect(positionOf(cy, 'right')).toEqual(before);
    expect(shifts.get('group')?.has('right')).toBe(true);
  });

  it('forgets a pushed node that disappeared, so it is not pulled when it comes back', () => {
    const { cy, shifts } = setup();

    cy.getElementById('right').remove();
    releaseRemovedGroups(cy, shifts);

    expect(shifts.get('group')?.has('right')).toBe(false);
  });
});

describe('placeGroupChanges: several groups change in one update', () => {
  // 두 group을 나란히 펼친 상태. 같은 행이라 서로를 밀어 둔다.
  const setup = () => {
    const cy = createCy();
    const shifts = new Map<string, ShiftRecord>();
    cy.add([
      { data: { id: 'g1' }, position: { x: 0, y: 0 } },
      { data: { id: 'g2' }, position: { x: 400, y: 0 } },
    ]);
    const expand = (groupId: string) => {
      const snapshot = snapshotNodes(cy);
      cy.getElementById(groupId).remove();
      const added = [
        cy.add({ data: { id: `${groupId}-box`, anchorId: groupId } }),
        ...Array.from({ length: 8 }, (_, i) =>
          cy.add({ data: { id: `${groupId}-c${i}`, parent: `${groupId}-box`, anchorId: groupId } }),
        ),
      ];
      placeGroupChanges(cy, added, snapshot, shifts);
    };
    expand('g1');
    expand('g2');
    return { cy, shifts };
  };

  const collapse = (cy: cytoscape.Core, shifts: Map<string, ShiftRecord>, groupId: string) => {
    const snapshot = snapshotNodes(cy);
    cy.getElementById(`${groupId}-box`).children().remove();
    cy.getElementById(`${groupId}-box`).remove();
    const added = [cy.add({ data: { id: groupId, anchorId: `${groupId}-box` } })];
    placeGroupChanges(cy, added, snapshot, shifts);
    releaseRemovedGroups(cy, shifts);
  };

  it('puts both groups back in place when children join both boxes at once', () => {
    const { cy, shifts } = setup();

    // 실시간 응답 하나에 두 상자의 새 자식이 함께 실린다. 한 칸에 8개까지 쌓이므로 둘 다 넓어진다.
    const snapshot = snapshotNodes(cy);
    ['g1', 'g2'].forEach((groupId) =>
      cy.add({
        data: { id: `${groupId}-c8`, parent: `${groupId}-box`, anchorId: groupId },
        position: { x: 0, y: 0 },
      }),
    );
    placeGroupChanges(cy, [], snapshot, shifts);

    collapse(cy, shifts, 'g1');
    collapse(cy, shifts, 'g2');

    expect(positionOf(cy, 'g1').x).toBeCloseTo(0);
    expect(positionOf(cy, 'g1').y).toBeCloseTo(0);
    expect(positionOf(cy, 'g2').x).toBeCloseTo(400);
    expect(positionOf(cy, 'g2').y).toBeCloseTo(0);
    expect(shifts.size).toBe(0);
  });
});
