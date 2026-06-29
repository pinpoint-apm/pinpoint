import { computeParallelGroups, isTimelineWorkRow } from './timeline';
import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';

type Row = TransactionInfo.CallStackKeyValueMap;

// minimal row factory — only the fields the timeline pure functions read
const row = (r: {
  id: number | string;
  parentId?: number | string | null;
  begin?: number;
  end?: number;
  apiType?: string;
  methodType?: number;
  excludeFromTimeline?: boolean;
  isMethod?: boolean;
  hasException?: boolean;
  exceptionChainId?: string;
}): Row =>
  ({
    parentId: null,
    begin: 0,
    end: 0,
    apiType: '',
    methodType: 0,
    excludeFromTimeline: false,
    isMethod: true,
    ...r,
  }) as unknown as Row;

describe('isTimelineWorkRow', () => {
  test('accepts method rows with a valid begin time', () => {
    expect(isTimelineWorkRow(row({ id: 1, begin: 1000, end: 1100 }))).toBe(true);
  });

  test('skips metadata and async invocation rows', () => {
    expect(isTimelineWorkRow(row({ id: 1, begin: 0, end: 0 }))).toBe(false);
    expect(isTimelineWorkRow(row({ id: 2, begin: 1000, end: 1100, isMethod: false }))).toBe(
      false,
    );
    expect(
      isTimelineWorkRow(row({ id: 3, begin: 1000, end: 1100, excludeFromTimeline: true })),
    ).toBe(false);
    expect(isTimelineWorkRow(row({ id: 4, begin: 1000, end: 1100, apiType: 'ASYNC' }))).toBe(
      false,
    );
    expect(isTimelineWorkRow(row({ id: 5, begin: 1000, end: 1100, methodType: 200 }))).toBe(
      false,
    );
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

  test('annotation (no begin) and excludeFromTimeline rows are skipped', () => {
    const rows = [
      row({ id: 1, parentId: null, begin: 1000, end: 1100 }),
      row({ id: 2, parentId: 1, begin: 0, end: 0 }), // annotation
      row({ id: 3, parentId: 1, begin: 1000, end: 1010, excludeFromTimeline: true }),
      row({ id: 4, parentId: 1, begin: 1000, end: 1010 }), // only this remains -> no group
    ];
    expect(computeParallelGroups(rows).size).toBe(0);
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
