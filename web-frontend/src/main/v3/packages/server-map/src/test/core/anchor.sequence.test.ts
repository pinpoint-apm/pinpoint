import cytoscape from 'cytoscape';
import {
  placeAnchoredNodes,
  relayoutChangedGroups,
  releaseRemovedGroups,
  ShiftRecord,
  snapshotNodes,
} from '../../core/anchor';

/**
 * 펼치기·접기·자식 늘고 줄기·group 사라졌다 다시 나타나기(실시간 갱신)를 무작위 순서로 섞어 돌린 뒤
 * 모두 접으면, 모든 노드가 제자리로 돌아와야 한다. 하나씩 해 보는 테스트로는 순서가 엇갈릴 때의
 * 문제(다른 group이 밀어 둔 기록이 사라지는 등)를 잡지 못해 리뷰에서야 드러났다.
 *
 * 매 동작은 `ServerMap`이 데이터가 바뀔 때 하는 일을 그대로 거친다(`update`): 바꾸기 전에 위치를 떠
 * 두고, 노드를 지우고 더한 뒤, anchor가 있는 노드는 `placeAnchoredNodes`로 놓고, 자식이 바뀐 상자는
 * `relayoutChangedGroups`로 다시 배치하고, 사라진 group은 `releaseRemovedGroups`로 정리한다.
 *
 * 제자리 = 처음 자리. 사라졌다 다시 나타난 group은 다시 나타난 자리다(`ServerMap`은 anchor 없는 노드를
 * 기준 노드 옆에 따로 놓으므로, 여기서는 처음 자리에 둔다).
 *
 * 실패하면 메시지의 seed와 동작 목록으로 그대로 재현할 수 있다.
 */

const createCy = () =>
  cytoscape({
    headless: true,
    styleEnabled: true,
    style: [
      { selector: 'node', style: { width: 100, height: 100 } },
      { selector: 'node:parent', style: { padding: '30px' } },
    ],
  });

// 재현할 수 있도록 seed로 정해지는 난수(mulberry32).
const createRandom = (seed: number) => {
  let a = seed;
  return () => {
    a = (a + 0x6d2b79f5) | 0;
    let t = Math.imul(a ^ (a >>> 15), 1 | a);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
};

// group 넷과 평범한 노드 넷. 같은 행·열에 서로 걸치도록 400px 간격의 격자에 둔다.
const INITIAL_POSITIONS: Record<string, { x: number; y: number }> = {
  g0: { x: 0, y: 0 },
  g1: { x: 400, y: 0 },
  g2: { x: 0, y: 400 },
  g3: { x: 400, y: 400 },
  n0: { x: 800, y: 0 },
  n1: { x: -400, y: 0 },
  n2: { x: 0, y: 800 },
  n3: { x: 800, y: 400 },
};
const GROUP_IDS = ['g0', 'g1', 'g2', 'g3'];

const boxIdOf = (groupId: string) => `${groupId}-box`;

const createWorld = () => {
  const cy = createCy();
  // cytoscape는 넘긴 position 객체를 그대로 쥐고 고쳐 쓰므로 복사해서 넘긴다(처음 위치가 바뀌지 않게).
  cy.add(
    Object.entries(INITIAL_POSITIONS).map(([id, position]) => ({
      data: { id },
      position: { ...position },
    })),
  );
  const shifts = new Map<string, ShiftRecord>();
  // group마다 지금 소속된 자식 id. 접혀 있어도 데이터로는 늘고 준다(실시간 갱신).
  const members = new Map(GROUP_IDS.map((id) => [id, [`${id}-c0`, `${id}-c1`, `${id}-c2`]]));
  let nextChild = 3;
  const isExpanded = (groupId: string) => cy.getElementById(boxIdOf(groupId)).nonempty();
  const isDrawn = (groupId: string) => cy.getElementById(groupId).nonempty() || isExpanded(groupId);

  /** 데이터가 바뀐 한 번. `change`가 노드를 지우고 더하며, 더한 노드를 돌려준다. */
  const update = (change: () => cytoscape.CollectionReturnValue[]) => {
    const snapshot = snapshotNodes(cy);
    const added = change();
    const anchored = added.filter((node) => snapshot.has(node.data('anchorId')));
    if (anchored.length > 0) {
      placeAnchoredNodes(cy, anchored, snapshot, shifts);
    }
    relayoutChangedGroups(cy, snapshot, shifts);
    releaseRemovedGroups(cy, shifts);
  };

  const removeGroup = (groupId: string) => {
    const box = cy.getElementById(boxIdOf(groupId));
    box.children().remove();
    box.remove();
    cy.getElementById(groupId).remove();
  };

  const expand = (groupId: string) =>
    update(() => {
      cy.getElementById(groupId).remove();
      return [
        cy.add({ data: { id: boxIdOf(groupId), anchorId: groupId } }),
        ...members
          .get(groupId)!
          .map((id) => cy.add({ data: { id, parent: boxIdOf(groupId), anchorId: groupId } })),
      ];
    });

  const collapse = (groupId: string) =>
    update(() => {
      removeGroup(groupId);
      return [cy.add({ data: { id: groupId, anchorId: boxIdOf(groupId) } })];
    });

  const join = (groupId: string) => {
    const id = `${groupId}-c${nextChild++}`;
    members.get(groupId)!.push(id);
    if (isExpanded(groupId)) {
      // ServerMap은 위치 없이 더한다(원점).
      update(() => [cy.add({ data: { id, parent: boxIdOf(groupId), anchorId: groupId } })]);
    }
  };

  const leave = (groupId: string, random: () => number) => {
    const list = members.get(groupId)!;
    if (list.length <= 1) {
      return;
    }
    const [id] = list.splice(Math.floor(random() * list.length), 1);
    if (isExpanded(groupId)) {
      update(() => {
        cy.getElementById(id).remove();
        return [];
      });
    }
  };

  // 그 service의 호출이 끊겨 group이 응답에서 통째로 빠진다. 펼쳐져 있었으면 상자째로 사라진다.
  // (ServerMapCore는 이때 펼친 상태도 지우므로, 다시 나타날 때는 접힌 채로 온다.)
  const vanish = (groupId: string) =>
    update(() => {
      removeGroup(groupId);
      return [];
    });

  const reappear = (groupId: string) =>
    update(() => [cy.add({ data: { id: groupId }, position: { ...INITIAL_POSITIONS[groupId] } })]);

  return { cy, shifts, isExpanded, isDrawn, expand, collapse, join, leave, vanish, reappear };
};

const OPERATIONS = ['toggle', 'toggle', 'toggle', 'join', 'leave', 'vanish'] as const;

const run = (seed: number, steps: number) => {
  const random = createRandom(seed);
  const world = createWorld();
  const log: string[] = [];

  for (let i = 0; i < steps; i++) {
    const groupId = GROUP_IDS[Math.floor(random() * GROUP_IDS.length)];
    const operation = OPERATIONS[Math.floor(random() * OPERATIONS.length)];

    if (!world.isDrawn(groupId)) {
      // 사라진 group은 다음에 뽑히면 다시 나타난다.
      world.reappear(groupId);
      log.push(`reappear ${groupId}`);
    } else if (operation === 'toggle') {
      if (world.isExpanded(groupId)) {
        world.collapse(groupId);
        log.push(`collapse ${groupId}`);
      } else {
        world.expand(groupId);
        log.push(`expand ${groupId}`);
      }
    } else if (operation === 'join') {
      world.join(groupId);
      log.push(`join ${groupId}`);
    } else if (operation === 'leave') {
      world.leave(groupId, random);
      log.push(`leave ${groupId}`);
    } else {
      world.vanish(groupId);
      log.push(`vanish ${groupId}`);
    }
  }

  // 남은 group을 무작위 순서로 모두 접는다.
  const remaining = GROUP_IDS.filter(world.isExpanded).sort(() => random() - 0.5);
  remaining.forEach((groupId) => {
    world.collapse(groupId);
    log.push(`collapse ${groupId}`);
  });

  return { ...world, log };
};

describe('placeAnchoredNodes / relayoutChangedGroups / releaseRemovedGroups in shuffled order', () => {
  const SEEDS = Array.from({ length: 200 }, (_, i) => i + 1);

  it.each(SEEDS)('puts every node back in place after collapsing everything (seed %i)', (seed) => {
    const { cy, shifts, log } = run(seed, 30);

    const moved = Object.entries(INITIAL_POSITIONS)
      .filter(([id]) => cy.getElementById(id).nonempty())
      .map(([id, initial]) => ({ id, initial, now: cy.getElementById(id).position() }))
      .filter(
        ({ initial, now }) =>
          Math.abs(now.x - initial.x) > 0.01 || Math.abs(now.y - initial.y) > 0.01,
      )
      .map(({ id, initial, now }) => `${id}: (${initial.x}, ${initial.y}) → (${now.x}, ${now.y})`);

    expect({ seed, moved, log: moved.length > 0 ? log : [] }).toEqual({ seed, moved: [], log: [] });
    expect(shifts.size).toBe(0);
  });
});
