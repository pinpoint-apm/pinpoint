import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';
import {
  collectSteppedInSpanIds,
  findSteppedInTreeNode,
  flattenTreeRows,
  getFlameGroupsTimeRange,
  pruneFlameGroupsToSteppedIn,
  rebaseTreeIndent,
} from './stepInSpan';
import { FlameNodeType } from '../FlameGraph/FlameNode';

type Row = TransactionInfo.CallStackKeyValueMap;

const row = (r: {
  id: number | string;
  parentId?: number | string | null;
  tab?: number;
  title?: string;
  subRows?: Row[];
}): Row => ({ parentId: null, tab: 0, title: '', ...r }) as unknown as Row;

// 1 ─ 2 ─ 4
//   └ 3
// 5 (unrelated sibling of 1)
const flatRows: Row[] = [
  row({ id: 1, parentId: null }),
  row({ id: 2, parentId: 1 }),
  row({ id: 3, parentId: 1 }),
  row({ id: 4, parentId: 2 }),
  row({ id: 5, parentId: null }),
];

describe('collectSteppedInSpanIds', () => {
  test('returns the stepped-into row plus every descendant', () => {
    expect(collectSteppedInSpanIds(flatRows, '2')).toEqual(new Set(['2', '4']));
  });

  test('returns the whole subtree when the trace root is stepped into', () => {
    expect(collectSteppedInSpanIds(flatRows, '1')).toEqual(new Set(['1', '2', '3', '4']));
  });

  test('returns only the row itself for a leaf', () => {
    expect(collectSteppedInSpanIds(flatRows, '4')).toEqual(new Set(['4']));
  });

  test('returns undefined when nothing is stepped into', () => {
    expect(collectSteppedInSpanIds(flatRows, '')).toBeUndefined();
    expect(collectSteppedInSpanIds(flatRows, undefined)).toBeUndefined();
  });

  test('returns undefined for an id that is not part of this trace, so the view is not blanked', () => {
    expect(collectSteppedInSpanIds(flatRows, '999')).toBeUndefined();
  });

  test('matches ids across number/string representations', () => {
    const numericIdRows = [row({ id: 1, parentId: null }), row({ id: 2, parentId: 1 })];
    expect(collectSteppedInSpanIds(numericIdRows, '1')).toEqual(new Set(['1', '2']));
  });
});

describe('findSteppedInTreeNode / rebaseTreeIndent', () => {
  const tree: Row[] = [
    row({
      id: 1,
      tab: 0,
      title: 'root',
      subRows: [
        row({ id: 2, tab: 1, title: 'child', subRows: [row({ id: 4, tab: 2, title: 'leaf' })] }),
        row({ id: 3, tab: 1, title: 'sibling' }),
      ],
    }),
  ];

  test('finds a nested node', () => {
    expect(findSteppedInTreeNode(tree, '4')?.title).toBe('leaf');
  });

  test('returns undefined for a row the tree does not render', () => {
    expect(findSteppedInTreeNode(tree, '99')).toBeUndefined();
  });

  test('re-bases indentation on the new root so it starts at the left edge', () => {
    const steppedInRoot = findSteppedInTreeNode(tree, '2') as Row;
    const rebased = rebaseTreeIndent(steppedInRoot);

    expect(rebased.tab).toBe(0);
    expect(rebased.subRows?.[0].tab).toBe(1);
    // the original tree is left untouched
    expect(steppedInRoot.tab).toBe(1);
    expect(steppedInRoot.subRows?.[0].tab).toBe(2);
  });

  test('lists the rendered rows in render order', () => {
    expect(flattenTreeRows(tree).map((r) => String(r.id))).toEqual(['1', '2', '4', '3']);
  });

  test('lists only what the tree draws, so search cannot match a row that is not there', () => {
    // `convertToTree` drops the backend's Attribute/Scope rows, lifting their values onto the
    // parent, so they must not appear here either.
    const withLifted = [row({ id: 1, title: 'method', subRows: [row({ id: 2, title: 'child' })] })];
    expect(flattenTreeRows(withLifted).map((r) => String(r.id))).toEqual(['1', '2']);
  });
});

describe('pruneFlameGroupsToSteppedIn', () => {
  const node = (
    id: string,
    start: number,
    duration: number,
    children: FlameNodeType<unknown>[] = [],
  ): FlameNodeType<unknown> => ({ id, name: id, start, duration, detail: {}, children });

  // group 0: 1 ─ 2 ─ 4      group 1 (async): 10
  const groups: FlameNodeType<unknown>[][] = [
    [node('1', 0, 100, [node('2', 10, 50, [node('4', 20, 10)]), node('3', 70, 20)])],
    [node('10', 30, 40)],
  ];

  test('re-roots the group at the stepped-into node and keeps async groups underneath it', () => {
    const pruned = pruneFlameGroupsToSteppedIn(groups, new Set(['2', '4', '10']));

    expect(pruned).toHaveLength(2);
    expect(pruned[0].map((n) => n.id)).toEqual(['2']);
    expect(pruned[0][0].children.map((n) => n.id)).toEqual(['4']);
    expect(pruned[1].map((n) => n.id)).toEqual(['10']);
  });

  test('drops groups that hold nothing stepped into', () => {
    const pruned = pruneFlameGroupsToSteppedIn(groups, new Set(['2', '4']));

    expect(pruned).toHaveLength(1);
    expect(pruned[0].map((n) => n.id)).toEqual(['2']);
  });

  test('returns no groups when the stepped-into span is not drawn here', () => {
    expect(pruneFlameGroupsToSteppedIn(groups, new Set(['999']))).toEqual([]);
  });

  test('spans the whole subtree, including a descendant that outlives its parent', () => {
    const pruned = pruneFlameGroupsToSteppedIn(groups, new Set(['2', '4', '10']));

    // node 2 ends at 60, but the async group's node 10 runs to 70
    expect(getFlameGroupsTimeRange(pruned)).toEqual({ start: 10, end: 70 });
  });

  test('has no time range without nodes', () => {
    expect(getFlameGroupsTimeRange([])).toBeUndefined();
  });
});
