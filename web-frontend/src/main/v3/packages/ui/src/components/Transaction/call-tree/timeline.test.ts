import { computeParallelGroups, getSteppedInTimelineAxis, isTimelineWorkRow } from './timeline';
import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';

type Row = TransactionInfo.CallStackKeyValueMap;

// minimal row factory — only the fields the timeline pure functions read
const row = (r: {
  id: number | string;
  parentId?: number | string | null;
  begin?: number;
  end?: number;
  beginOffsetNanos?: number | null;
  endOffsetNanos?: number | null;
  apiType?: string;
  methodType?: number;
  excludeFromTimeline?: boolean;
  isMethod?: boolean;
  hasException?: boolean;
  exceptionChainId?: string;
}): Row => {
  const beginOffsetNanos = r.beginOffsetNanos === undefined ? (r.begin ?? 0) : r.beginOffsetNanos;
  const endOffsetNanos = r.endOffsetNanos === undefined ? (r.end ?? 0) : r.endOffsetNanos;
  return {
    parentId: null,
    begin: 0,
    end: 0,
    beginOffsetNanos,
    endOffsetNanos,
    apiType: '',
    methodType: 0,
    excludeFromTimeline: false,
    isMethod: true,
    ...r,
  } as unknown as Row;
};

describe('isTimelineWorkRow', () => {
  test('accepts method rows with valid timeline offsets', () => {
    expect(isTimelineWorkRow(row({ id: 1, begin: 1000, end: 1100 }))).toBe(true);
  });

  test('skips metadata and async invocation rows', () => {
    expect(isTimelineWorkRow(row({ id: 1, begin: 0, end: 0, beginOffsetNanos: null }))).toBe(false);
    expect(isTimelineWorkRow(row({ id: 2, begin: 1000, end: 1100, isMethod: false }))).toBe(false);
    expect(isTimelineWorkRow(row({ id: 4, begin: 1000, end: 1100, apiType: 'ASYNC' }))).toBe(false);
    expect(isTimelineWorkRow(row({ id: 5, begin: 1000, end: 1100, methodType: 200 }))).toBe(false);
  });

  test('accepts excludeFromTimeline (INTERNAL_METHOD) rows — they are executed work', () => {
    // The server flags INTERNAL_METHOD rows for the RPC Timeline view; the Call Tree still
    // draws their bars (OTel kind-unclassified spans map entirely to INTERNAL_METHOD).
    expect(
      isTimelineWorkRow(row({ id: 3, begin: 1000, end: 1100, excludeFromTimeline: true })),
    ).toBe(true);
  });
});

describe('computeParallelGroups', () => {
  test('returns empty map for empty input', () => {
    expect(computeParallelGroups(undefined).size).toBe(0);
    expect(computeParallelGroups([]).size).toBe(0);
  });

  test('sequential (non-overlapping) siblings form no group', () => {
    const rows = [
      row({ id: 1, parentId: null, begin: 1000, end: 1100 }),
      row({ id: 2, parentId: 1, begin: 1000, end: 1010 }),
      row({ id: 3, parentId: 1, begin: 1010, end: 1020 }), // starts exactly when id2 ends
    ];
    expect(computeParallelGroups(rows).size).toBe(0);
  });

  test('overlapping siblings form one group spanning their union window', () => {
    const rows = [
      row({ id: 1, parentId: null, begin: 1000, end: 1100 }),
      row({ id: 2, parentId: 1, begin: 1000, end: 1010 }),
      row({ id: 3, parentId: 1, begin: 1005, end: 1015 }),
      row({ id: 4, parentId: 1, begin: 1012, end: 1014 }), // overlaps id3 (1012 < 1015)
    ];
    const g = computeParallelGroups(rows);
    expect(g.size).toBe(3);
    expect(g.get('2')).toEqual({ group: { start: 1000, end: 1015, size: 3 }, isFirst: true });
    expect(g.get('3')?.isFirst).toBe(false);
    expect(g.get('4')?.group.size).toBe(3);
    // the root (only child of its parent) is never grouped
    expect(g.get('1')).toBeUndefined();
  });

  test('async-invocation dividers (apiType ASYNC / methodType 200) are excluded', () => {
    const rows = [
      row({ id: 1, parentId: null, begin: 1000, end: 1100 }),
      row({ id: 2, parentId: 1, begin: 1000, end: 1010, apiType: 'ASYNC', methodType: 200 }),
      row({ id: 3, parentId: 1, begin: 1000, end: 1010 }), // overlaps id2 in time, but id2 is skipped
    ];
    expect(computeParallelGroups(rows).size).toBe(0);
  });

  test('annotation (no begin) rows are skipped; excludeFromTimeline rows participate', () => {
    const rows = [
      row({ id: 1, parentId: null, begin: 1000, end: 1100 }),
      row({ id: 2, parentId: 1, begin: 0, end: 0, isMethod: false }), // annotation — skipped
      row({ id: 3, parentId: 1, begin: 1000, end: 1010, excludeFromTimeline: true }),
      row({ id: 4, parentId: 1, begin: 1005, end: 1015 }), // overlaps id3 -> group of 2
    ];
    const g = computeParallelGroups(rows);
    expect(g.size).toBe(2);
    expect(g.get('3')?.group).toEqual({ start: 1000, end: 1015, size: 2 });
    expect(g.get('2')).toBeUndefined();
  });

  test('exception detail rows are skipped', () => {
    const rows = [
      row({ id: 1, parentId: null, begin: 1000, end: 1100 }),
      row({
        id: 2,
        parentId: 1,
        begin: 1000,
        end: 1010,
        isMethod: false,
        hasException: true,
        exceptionChainId: '4',
      }),
      row({ id: 3, parentId: 1, begin: 1005, end: 1015 }),
    ];
    expect(computeParallelGroups(rows).size).toBe(0);
  });

  test('overlap is scoped per parent (different parents do not group together)', () => {
    const rows = [
      row({ id: 1, parentId: null, begin: 1000, end: 1100 }),
      row({ id: 2, parentId: 1, begin: 1000, end: 1010 }),
      row({ id: 3, parentId: 2, begin: 1000, end: 1010 }), // child of id2, not a sibling
    ];
    expect(computeParallelGroups(rows).size).toBe(0);
  });
});

describe('getSteppedInTimelineAxis', () => {
  // 1 (0..1000) ─ 2 (200..600) ─ 4 (300..500)
  //             └ 3 (700..900)
  const rows = [
    row({ id: 1, parentId: null, begin: 0, end: 1000 }),
    row({ id: 2, parentId: 1, begin: 200, end: 600 }),
    row({ id: 4, parentId: 2, begin: 300, end: 500 }),
    row({ id: 3, parentId: 1, begin: 700, end: 900 }),
  ];

  test('spans exactly the stepped-into subtree so it fills the column', () => {
    expect(getSteppedInTimelineAxis(rows, new Set(['2', '4']))).toEqual({
      startNanos: 200,
      durationNanos: 400,
    });
  });

  test('covers a descendant that outlives its parent (async work)', () => {
    const asyncRows = [
      row({ id: 1, parentId: null, begin: 0, end: 100 }),
      row({ id: 2, parentId: 1, begin: 50, end: 400 }),
    ];
    expect(getSteppedInTimelineAxis(asyncRows, new Set(['1', '2']))).toEqual({
      startNanos: 0,
      durationNanos: 400,
    });
  });

  test('falls back (undefined) with nothing stepped into, no rows, or no measurable window', () => {
    expect(getSteppedInTimelineAxis(rows, undefined)).toBeUndefined();
    expect(getSteppedInTimelineAxis(rows, new Set())).toBeUndefined();
    expect(getSteppedInTimelineAxis(undefined, new Set(['2']))).toBeUndefined();
    // a single zero-duration row leaves nothing to scale to
    expect(
      getSteppedInTimelineAxis([row({ id: 9, begin: 500, end: 500 })], new Set(['9'])),
    ).toBeUndefined();
  });

  test('ignores rows that are not timeline work', () => {
    const withMetadata = [
      row({ id: 1, parentId: null, begin: 100, end: 300 }),
      row({ id: 2, parentId: 1, begin: 0, end: 900, isMethod: false }), // annotation row
    ];
    expect(getSteppedInTimelineAxis(withMetadata, new Set(['1', '2']))).toEqual({
      startNanos: 100,
      durationNanos: 200,
    });
  });
});
