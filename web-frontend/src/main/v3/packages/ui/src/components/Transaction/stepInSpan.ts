import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { FlameNodeType } from '../FlameGraph/FlameNode';

/**
 * Helpers for "Step in": re-root the Call Tree / Flame Graph at one span so only that span and
 * its descendants are drawn. Stepping in only changes what is displayed — it neither refetches
 * nor alters the underlying trace.
 *
 * The two tabs share one stepped-into id: the trace viewer's `args.id` is the same call stack
 * record id as the Call Tree row's `id` (the backend writes `record.getId()` into both). The
 * descendant set is therefore always derived from the flat call stack (`mapData`), which is the
 * only view that holds the complete parent/child relation — the flame graph splits async work
 * into separate `tid` groups and would otherwise lose those descendants.
 *
 * Kept free of React imports so it can be unit-tested in isolation.
 */

/** Ids of the stepped-into row plus every descendant. `undefined` when nothing is stepped into. */
export const collectSteppedInSpanIds = (
  rows: TransactionInfo.CallStackKeyValueMap[] | undefined,
  steppedInSpanId: string | undefined,
): Set<string> | undefined => {
  if (!steppedInSpanId || !rows?.length) {
    return undefined;
  }

  const childIdsByParentId = new Map<string, string[]>();
  let steppedInExists = false;
  rows.forEach((row) => {
    const id = String(row.id);
    if (id === steppedInSpanId) {
      steppedInExists = true;
    }
    const parentId = String(row.parentId);
    const siblings = childIdsByParentId.get(parentId);
    if (siblings) {
      siblings.push(id);
    } else {
      childIdsByParentId.set(parentId, [id]);
    }
  });

  // An id that is not part of this trace (e.g. left over from another transaction) must not
  // blank out the view.
  if (!steppedInExists) {
    return undefined;
  }

  const steppedInIds = new Set<string>([steppedInSpanId]);
  const queue = [steppedInSpanId];
  while (queue.length) {
    const id = queue.pop() as string;
    for (const childId of childIdsByParentId.get(id) ?? []) {
      if (!steppedInIds.has(childId)) {
        steppedInIds.add(childId);
        queue.push(childId);
      }
    }
  }

  return steppedInIds;
};

/** The stepped-into node inside the Call Tree row tree, or `undefined` when it is not rendered. */
export const findSteppedInTreeNode = (
  rows: TransactionInfo.CallStackKeyValueMap[] | undefined,
  steppedInSpanId: string | undefined,
): TransactionInfo.CallStackKeyValueMap | undefined => {
  if (!steppedInSpanId || !rows?.length) {
    return undefined;
  }

  for (const row of rows) {
    if (String(row.id) === steppedInSpanId) {
      return row;
    }
    const found = findSteppedInTreeNode(row.subRows, steppedInSpanId);
    if (found) {
      return found;
    }
  }

  return undefined;
};

/**
 * Copies a subtree with its indentation re-based on the new root, so a deeply nested row starts
 * at the left edge instead of being pushed off-screen (`tab` drives the row's padding).
 */
export const rebaseTreeIndent = (
  node: TransactionInfo.CallStackKeyValueMap,
  indentOffset = Number(node.tab) || 0,
): TransactionInfo.CallStackKeyValueMap => ({
  ...node,
  tab: Math.max((Number(node.tab) || 0) - indentOffset, 0),
  subRows: node.subRows?.map((subRow) => rebaseTreeIndent(subRow, indentOffset)),
});

/**
 * The rows the fully expanded table draws, in render order. This is the authoritative "what is on
 * screen" list: it excludes the flat call stack's Attribute/Scope rows (the tree lifts those onto
 * their parent) as well as anything outside the stepped-into subtree.
 */
export const flattenTreeRows = (
  rows: TransactionInfo.CallStackKeyValueMap[] | undefined,
): TransactionInfo.CallStackKeyValueMap[] => {
  const flat: TransactionInfo.CallStackKeyValueMap[] = [];
  const walk = (nodes: TransactionInfo.CallStackKeyValueMap[] | undefined) => {
    nodes?.forEach((node) => {
      flat.push(node);
      walk(node.subRows);
    });
  };
  walk(rows);
  return flat;
};

/**
 * Keeps only the stepped-into part of the flame graph. For every `tid` group the topmost nodes
 * that belong to the set become the group's new roots, so the stepped-into span heads its own
 * group while async groups spawned underneath it survive as their own groups. Groups left with
 * nothing are dropped.
 */
export const pruneFlameGroupsToSteppedIn = <T>(
  groups: FlameNodeType<T>[][],
  steppedInIds: Set<string>,
): FlameNodeType<T>[][] => {
  const collect = (node: FlameNodeType<T>): FlameNodeType<T>[] =>
    steppedInIds.has(String(node.id)) ? [node] : (node.children ?? []).flatMap(collect);

  return groups.map((roots) => roots.flatMap(collect)).filter((roots) => roots.length > 0);
};

/**
 * Time window covered by the given flame nodes, so the stepped-into subtree fills the timeline.
 * Spans the whole subtree rather than only its root: an async descendant may outlive its parent
 * and would otherwise be drawn past the right edge.
 */
export const getFlameGroupsTimeRange = <T>(
  groups: FlameNodeType<T>[][],
): { start: number; end: number } | undefined => {
  let start = Number.POSITIVE_INFINITY;
  let end = Number.NEGATIVE_INFINITY;

  const walk = (node: FlameNodeType<T>) => {
    if (Number.isFinite(node.start)) {
      start = Math.min(start, node.start);
      end = Math.max(end, node.start + (Number.isFinite(node.duration) ? node.duration : 0));
    }
    (node.children ?? []).forEach(walk);
  };
  groups.forEach((roots) => roots.forEach(walk));

  if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) {
    return undefined;
  }
  return { start, end };
};
