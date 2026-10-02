import cytoscape from 'cytoscape';
import { GraphStyle } from '../constants/style/theme';

/** 부모(compound) 안에서 자식들을 놓을 때의 열 간격(px). 안쪽 링크의 라벨이 들어갈 자리를 둔다. */
const CHILD_COLUMN_GAP = GraphStyle.NODE_WIDTH + GraphStyle.RANK_SEP / 2;
/** 부모(compound) 안에서 자식들을 놓을 때의 행 간격(px). 노드 아래 라벨이 들어갈 자리를 둔다. */
const CHILD_ROW_GAP = GraphStyle.NODE_HEIGHT + GraphStyle.NODE_GAP * 2;
/** 한 칸(block)에 쌓는 최소 행 수. 이보다 적으면 줄을 바꾸지 않고 한 칸에 쌓는다. */
const MIN_ROWS_PER_BLOCK = 8;

export interface NodeSnapshot {
  x: number;
  y: number;
  w: number;
  h: number;
  parent?: string;
}

/** 펼칠 때 주변 노드를 비켜 세운 양. 다시 묶을 때 그만큼 되돌린다. (노드 id → 이동량) */
export type ShiftRecord = Map<string, { dx: number; dy: number }>;

export const snapshotNodes = (cy: cytoscape.Core) => {
  const snapshot = new Map<string, NodeSnapshot>();
  cy.nodes().forEach((node) => {
    const { x, y } = node.position();
    const { w, h } = node.boundingBox();
    snapshot.set(node.id(), { x, y, w, h, parent: node.data('parent') });
  });
  return snapshot;
};

/**
 * 부모 안의 자식들을 왼쪽→오른쪽 흐름으로 놓을 상대 좌표를 정한다. 가운데가 (0, 0)이다.
 *
 * 전체 map과 같은 방향(dagre `rankDir: 'LR'`)이 되도록, 안쪽 링크를 따라 몇 단계 뒤에 있는지로
 * 열을 정하고 같은 열은 세로로 쌓는다. 순환이 있어도 끝나도록 단계 계산은 노드 수만큼만 돈다.
 *
 * 자식이 많으면 열이 한없이 길어져 상자가 막대처럼 된다(200개면 높이가 16,000px을 넘어 최소
 * 배율로도 화면에 다 들어오지 않고, 옆 노드들이 화면 밖으로 밀려난다). 그래서 행이 넘치면 **모든
 * 열을 함께** 끊어 오른쪽 칸(block)으로 넘긴다 — 바둑판처럼. 열마다 따로 끊지 않는 이유는 같은
 * 행에서 이어지던 호출자·피호출자(`b-1 → b-2`)가 칸이 갈려 멀어지지 않게 하기 위해서다.
 * 칸의 행 수는 상자가 정사각형에 가까워지도록 정한다(최소 `MIN_ROWS_PER_BLOCK`).
 *
 * cytoscape layout을 쓰지 않는 이유: layout은 `layoutready`를 내보내고, 그 이벤트는 화면을 기준
 * 노드로 다시 센터링한다(`ServerMap`). 펼칠 때마다 화면이 튀게 된다.
 */
export const layoutGroupChildren = (ids: string[], edges: [string, string][]) => {
  const idSet = new Set(ids);
  const innerEdges = edges.filter(
    ([source, target]) => source !== target && idSet.has(source) && idSet.has(target),
  );
  const rank = new Map(ids.map((id) => [id, 0]));

  for (let i = 0; i < ids.length; i++) {
    let changed = false;
    innerEdges.forEach(([source, target]) => {
      const next = rank.get(source)! + 1;
      if (next > rank.get(target)! && next < ids.length) {
        rank.set(target, next);
        changed = true;
      }
    });
    if (!changed) {
      break;
    }
  }

  const columns = new Map<number, string[]>();
  ids.forEach((id) => {
    const r = rank.get(id)!;
    columns.set(r, [...(columns.get(r) ?? []), id]);
  });
  const rankCount = Math.max(...columns.keys()) + 1;
  const maxLength = Math.max(...[...columns.values()].map((column) => column.length));

  // 칸 수 = maxLength / rows 이므로 너비 ≈ (maxLength / rows) × rankCount × 열 간격,
  // 높이 = rows × 행 간격. 둘이 같아지는 rows를 고른다.
  const rowsPerBlock = Math.max(
    MIN_ROWS_PER_BLOCK,
    Math.ceil(Math.sqrt((maxLength * rankCount * CHILD_COLUMN_GAP) / CHILD_ROW_GAP)),
  );
  const blockCount = Math.ceil(maxLength / rowsPerBlock);
  // 열이 둘 이상이면 칸 사이를 더 띄워 어느 열끼리 한 칸인지 보이게 한다.
  const blockStride = rankCount * CHILD_COLUMN_GAP + (rankCount > 1 ? CHILD_COLUMN_GAP / 2 : 0);
  const width = (blockCount - 1) * blockStride + (rankCount - 1) * CHILD_COLUMN_GAP;

  const positions: Record<string, { x: number; y: number }> = {};
  columns.forEach((column, r) => {
    column.forEach((id, i) => {
      const block = Math.floor(i / rowsPerBlock);
      const row = i % rowsPerBlock;
      // 이 칸에 들어간 이 열의 노드 수. 칸을 다 채우지 못한 열(마지막 칸, 짧은 열)은 가운데 정렬한다.
      const segmentLength = Math.min(rowsPerBlock, column.length - block * rowsPerBlock);
      positions[id] = {
        x: block * blockStride + r * CHILD_COLUMN_GAP - width / 2,
        y: (row - (segmentLength - 1) / 2) * CHILD_ROW_GAP,
      };
    });
  });
  return positions;
};

/**
 * 커진 상자와 부딪히는 노드들을 바깥으로 민다.
 *
 * 상자와 같은 열(가로 범위가 겹침)에 있는 노드는 위아래로, 같은 행(세로 범위가 겹침)에 있는 노드는
 * 좌우로 커진 만큼의 절반씩 민다. 부딪히는 노드만 밀면 그 너머 노드와 겹치므로 같은 열·행의 한쪽
 * 편은 모두 같은 양만큼 민다 — 상자와 무관한 노드는 제자리에 두고, 부딪히는 줄에서만 틈이 벌어진다.
 * 다른 부모(compound)는 상자째로 판단하고 자식째로 옮긴다(상자가 찢어지지 않도록).
 */
const shiftAround = (
  cy: cytoscape.Core,
  anchor: { x: number; y: number },
  box: cytoscape.BoundingBox12 & cytoscape.BoundingBoxWH,
  exclude: cytoscape.NodeCollection,
  halfW: number,
  halfH: number,
): ShiftRecord => {
  const record: ShiftRecord = new Map();
  const sign = (value: number) => (value > 0 ? 1 : value < 0 ? -1 : 0);

  cy.nodes()
    .orphans()
    .difference(exclude)
    .forEach((unit) => {
      const { x, y } = unit.position();
      const bb = unit.boundingBox();
      const isSameColumn = bb.x1 < box.x2 && bb.x2 > box.x1;
      const isSameRow = bb.y1 < box.y2 && bb.y2 > box.y1;
      const dx = isSameRow ? sign(x - anchor.x) * halfW : 0;
      const dy = isSameColumn ? sign(y - anchor.y) * halfH : 0;
      if (dx === 0 && dy === 0) {
        return;
      }
      const targets: cytoscape.NodeCollection = unit.isParent()
        ? unit.descendants(':childless')
        : unit;
      targets.forEach((node) => {
        node.shift({ x: dx, y: dy });
        record.set(node.id(), { dx, dy });
      });
    });

  return record;
};

/** 부모 상자가 있던 자리. 상자의 가운데는 자식들의 라벨까지 포함해 치우치므로 자식 위치의 가운데를 쓴다. */
const getChildrenCenter = (prevSnapshot: Map<string, NodeSnapshot>, parentId: string) => {
  const children = [...prevSnapshot.values()].filter(({ parent }) => parent === parentId);
  if (children.length === 0) {
    return prevSnapshot.get(parentId)!;
  }
  const xs = children.map(({ x }) => x);
  const ys = children.map(({ y }) => y);
  return {
    x: (Math.min(...xs) + Math.max(...xs)) / 2,
    y: (Math.min(...ys) + Math.max(...ys)) / 2,
  };
};

/**
 * 다른 group이 펼쳐지거나 묶이며 노드가 바뀌어도, 그 group을 밀어 둔 기록이 새 노드를 가리키게 옮긴다.
 *
 * 기록은 노드 id로 적으므로, A를 펼치며 B를 밀어 둔 뒤 B를 펼치면 기록 속의 B가 사라진다(반대로
 * B의 자식 id로 적힌 뒤 B를 묶어도 같다). 그대로 두면 A를 묶을 때 B를 되돌리지 못해 B가 밀린 채로
 * 남는다. 한 group은 언제나 통째로(같은 양만큼) 밀리므로 묶음 노드 ↔ 자식들로 그대로 옮겨 적는다.
 */
const migrateShifts = (shifts: Map<string, ShiftRecord>, fromIds: string[], toIds: string[]) => {
  shifts.forEach((record) => {
    const shift = fromIds.map((id) => record.get(id)).find(Boolean);
    if (!shift) {
      return;
    }
    fromIds.forEach((id) => record.delete(id));
    toIds.forEach((id) => record.set(id, shift));
  });
};

/** 상자 안 자식들을 `center`를 가운데로 다시 놓는다. */
const placeChildren = (children: cytoscape.NodeCollection, center: { x: number; y: number }) => {
  const relative = layoutGroupChildren(
    children.map((child) => child.id()),
    children.edgesWith(children).map((edge) => [edge.source().id(), edge.target().id()]),
  );
  children.forEach((child) => {
    const { x, y } = relative[child.id()];
    child.position({ x: center.x + x, y: center.y + y });
  });
};

/**
 * 놓은 뒤 다른 group에 밀려 옮겨졌을 수 있는 가운데. 상자는 언제나 통째로 밀리므로 자식 하나가
 * 움직인 만큼 가운데도 움직였다.
 */
const trackCenter = (nodes: cytoscape.NodeCollection, center: { x: number; y: number }) => {
  const reference = nodes.first();
  const placed = { ...reference.position() };
  return () => {
    const now = reference.position();
    return { x: center.x + now.x - placed.x, y: center.y + now.y - placed.y };
  };
};

/**
 * 데이터가 바뀐 한 번에 생긴 group의 변화를 모두 반영한다. 셋 다 같은 갱신에 함께 실릴 수 있다.
 *
 * - 펼침(`anchorId`가 바로 전에 그려져 있던 부모 노드가 생김): 자식들을 group 노드가 있던 자리를
 *   가운데로 배치하고, 상자가 커진 만큼 주변 노드를 비켜 세운다. 비켜 세운 양은 group id로 `shifts`에
 *   적어 둔다.
 * - 묶음(`anchorId`가 바로 전에 그려져 있던 상자인 노드가 생김): 상자가 있던 자리(그때 자식들의
 *   가운데)에 놓고, 펼칠 때 적어 둔 양만큼 주변 노드를 되돌린다.
 * - 이미 펼친 상자의 자식이 늘거나 줆(실시간 보기에서 펼쳐 둔 group의 application이 바뀜):
 *   자식들을 원래 가운데를 기준으로 다시 배치하고, 상자가 더 커졌으면 그만큼 주변을 더 비켜 세운다.
 *   - 늘었을 때: 새 자식은 anchor(접힌 group 노드)가 지금 그래프에 없어 위처럼 놓을 수 없다. 그대로
 *     두면 원점 근처에 놓여 상자를 길게 늘인다.
 *   - 줄었을 때: 그대로 두면 남은 자식들의 가운데가 한쪽으로 쏠린다. 접을 때 group은 그 가운데에
 *     놓이므로(`getChildrenCenter`) 펼치기 전 자리로 돌아오지 못한다.
 *   - 더 비켜 세운 양은 그 group의 기록에 더해 두어 묶을 때 함께 되돌린다. 줄었을 때 주변을 다시
 *     당기지는 않는다 — 틈이 조금 넓어질 뿐이고, 기록은 그대로라 묶으면 정확히 되돌아온다.
 *
 * **두 단계로 한다 — 먼저 모두 제자리에 놓고, 그다음 모두 민다.** 놓기는 바로 전 위치(`prevSnapshot`)를
 * 기준으로 한 절대 위치라서, 한 group씩 놓고 밀기를 번갈아 하면 뒤에 놓은 group이 앞의 group에 밀린
 * 것을 지워 버린다. 그런데 앞 group의 기록에는 민 것으로 남아, 앞 group을 묶을 때 뒤 group을 밀린
 * 적 없는 만큼 끌어당긴다. 밀기와 되돌리기는 모두 상대 이동이고 기록과 함께 움직이므로 그 순서는
 * 결과를 바꾸지 않는다. 기록을 새 id로 옮겨 적는 것(`migrateShifts`)도 놓을 때 모두 끝내 둔다 — 그래야
 * 밀 때 어느 기록이든 지금 그려진 노드를 가리킨다.
 */
export const placeGroupChanges = (
  cy: cytoscape.Core,
  anchoredNodes: cytoscape.CollectionReturnValue[],
  prevSnapshot: Map<string, NodeSnapshot>,
  shifts: Map<string, ShiftRecord>,
) => {
  // 1단계에서 놓으며 쌓고, 2단계에서 차례로 부른다.
  const pushes: (() => void)[] = [];

  const byAnchor = new Map<string, cytoscape.CollectionReturnValue[]>();
  anchoredNodes.forEach((node) => {
    const anchorId = node.data('anchorId') as string;
    byAnchor.set(anchorId, [...(byAnchor.get(anchorId) ?? []), node]);
  });

  byAnchor.forEach((nodes, anchorId) => {
    const anchor = prevSnapshot.get(anchorId)!;
    const parent = nodes.find((node) => node.isParent());

    if (parent) {
      const children = parent.children();
      placeChildren(children, anchor);
      migrateShifts(
        shifts,
        [anchorId],
        children.map((child) => child.id()),
      );

      const getCenter = trackCenter(children, anchor);
      pushes.push(() => {
        const box = parent.boundingBox();
        shifts.set(
          anchorId,
          shiftAround(
            cy,
            getCenter(),
            box,
            parent.union(children),
            Math.max(0, box.w - anchor.w) / 2,
            Math.max(0, box.h - anchor.h) / 2,
          ),
        );
      });
    } else {
      const center = getChildrenCenter(prevSnapshot, anchorId);
      const childIds = [...prevSnapshot.entries()]
        .filter(([, { parent }]) => parent === anchorId)
        .map(([id]) => id);
      nodes.forEach((node) => {
        node.position({ ...center });
        migrateShifts(shifts, childIds, [node.id()]);

        pushes.push(() => {
          shifts.get(node.id())?.forEach(({ dx, dy }, id) => {
            const target = cy.getElementById(id);
            if (target.nonempty() && target.isNode() && target.isChildless()) {
              target.shift({ x: -dx, y: -dy });
            }
          });
          shifts.delete(node.id());
        });
      });
    }
  });

  // 이미 펼쳐져 있던 상자. 방금 펼친 상자는 바로 전 그래프에 없으므로 위에서 놓았다.
  cy.nodes(':parent').forEach((parent) => {
    const parentId = parent.id();
    const prevBox = prevSnapshot.get(parentId);
    if (!prevBox) {
      return;
    }

    const prevChildIds = [...prevSnapshot.entries()]
      .filter(([, { parent }]) => parent === parentId)
      .map(([id]) => id);
    const children = parent.children();
    const childIds = children.map((child) => child.id());
    const isUnchanged =
      childIds.length === prevChildIds.length &&
      childIds.every((id) => prevSnapshot.get(id)?.parent === parentId);
    if (isUnchanged) {
      return;
    }

    // 빠진 자식까지 포함한 가운데 — 곧 펼치기 전 group이 있던 자리다.
    const center = getChildrenCenter(prevSnapshot, parentId);
    // 다른 group이 이 group을 밀어 둔 기록은 지금 자식들의 id로 옮겨 적는다. 안 하면 그 group을 묶을 때
    // 새 자식만 밀린 채 남아 상자가 찢어진다.
    migrateShifts(shifts, prevChildIds, childIds);
    placeChildren(children, center);

    const getCenter = trackCenter(children, center);
    pushes.push(() => {
      const box = parent.boundingBox();
      const grown = shiftAround(
        cy,
        getCenter(),
        box,
        parent.union(children),
        Math.max(0, box.w - prevBox.w) / 2,
        Math.max(0, box.h - prevBox.h) / 2,
      );

      const groupId = parent.data('anchorId') as string;
      const record: ShiftRecord = shifts.get(groupId) ?? new Map();
      grown.forEach(({ dx, dy }, id) => {
        const prev = record.get(id);
        record.set(id, { dx: dx + (prev?.dx ?? 0), dy: dy + (prev?.dy ?? 0) });
      });
      shifts.set(groupId, record);
    });
  });

  pushes.forEach((push) => push());
};

/**
 * 그래프에서 사라진 group이 밀어 둔 노드를 되돌리고, 사라진 노드를 가리키는 기록을 지운다.
 * 실시간 보기에서 펼쳐 둔 group이 통째로 응답에서 빠진 경우다.
 *
 * - 사라진 group의 기록: 묶으면서 되돌릴 기회가 없으므로 지금 되돌린다. 안 하면 그 옆 노드들이 밀린
 *   채로 남는다.
 * - 다른 기록 속의 사라진 노드: 지워 둔다. 같은 id로 다시 나타난 노드는 새로 놓인 것이라, 남겨 두면
 *   그 기록의 group을 묶을 때 밀린 적 없는 노드를 끌어당긴다.
 *
 * 펼친 group은 그 상자(`anchorId`가 group id인 최상위 노드)가 있는지로 본다. 기록은 펼쳐진 group에만
 * 있으므로 group 노드 자체가 있는 경우는 없지만, 같은 데이터 갱신에서 접힌 경우를 위해 함께 본다.
 * `placeGroupChanges`가 끝난 뒤에 불러야 한다 — 그것이 기록을 새 id로 옮겨 적기 전에는 옮겨질 id가
 * 사라진 것처럼 보인다.
 */
export const releaseRemovedGroups = (cy: cytoscape.Core, shifts: Map<string, ShiftRecord>) => {
  shifts.forEach((record, groupId) => {
    const isDrawn =
      cy.getElementById(groupId).nonempty() ||
      cy
        .nodes()
        .orphans()
        .some((node) => node.data('anchorId') === groupId);
    if (isDrawn) {
      return;
    }
    record.forEach(({ dx, dy }, id) => {
      const target = cy.getElementById(id);
      if (target.nonempty() && target.isNode() && target.isChildless()) {
        target.shift({ x: -dx, y: -dy });
      }
    });
    shifts.delete(groupId);
  });

  shifts.forEach((record) => {
    record.forEach((_, id) => {
      if (cy.getElementById(id).empty()) {
        record.delete(id);
      }
    });
  });
};
