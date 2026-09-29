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
 * 새로 생긴 노드 중 `anchorId`가 바로 전에 그려져 있던 노드들을 그 자리에 놓는다.
 *
 * - 펼침(부모 노드가 함께 생김): 자식들을 기준 노드가 있던 자리를 가운데로 배치하고, 상자가
 *   커진 만큼 주변 노드를 비켜 세운다. 비켜 세운 양은 기준 노드 id로 `shifts`에 적어 둔다.
 * - 묶음(노드 하나): 부모가 있던 자리(지금 자식들의 가운데)에 놓고, 펼칠 때 적어 둔 양만큼
 *   주변 노드를 되돌린다.
 */
export const placeAnchoredNodes = (
  cy: cytoscape.Core,
  anchoredNodes: cytoscape.CollectionReturnValue[],
  prevSnapshot: Map<string, NodeSnapshot>,
  shifts: Map<string, ShiftRecord>,
) => {
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
      const relative = layoutGroupChildren(
        children.map((child) => child.id()),
        children.edgesWith(children).map((edge) => [edge.source().id(), edge.target().id()]),
      );
      children.forEach((child) => {
        const { x, y } = relative[child.id()];
        child.position({ x: anchor.x + x, y: anchor.y + y });
      });

      const box = parent.boundingBox();
      shifts.set(
        anchorId,
        shiftAround(
          cy,
          anchor,
          box,
          parent.union(children),
          Math.max(0, box.w - anchor.w) / 2,
          Math.max(0, box.h - anchor.h) / 2,
        ),
      );
    } else {
      const { x, y } = getChildrenCenter(prevSnapshot, anchorId);
      nodes.forEach((node) => {
        node.position({ x, y });

        shifts.get(node.id())?.forEach(({ dx, dy }, id) => {
          const target = cy.getElementById(id);
          if (target.nonempty() && target.isNode() && target.isChildless()) {
            target.shift({ x: -dx, y: -dy });
          }
        });
        shifts.delete(node.id());
      });
    }
  });
};
