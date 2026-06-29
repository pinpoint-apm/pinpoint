import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';

// Pure helpers for the Call Tree "Timeline" column. Kept free of React / component imports so
// they can be unit-tested in isolation. The timeline axis itself is supplied by the server.
const rowStartMs = (r: TransactionInfo.CallStackKeyValueMap): number => Number(r.begin);
const rowEndMs = (r: TransactionInfo.CallStackKeyValueMap): number => Number(r.end);

export type TimelineAxis = { start: number; end: number };

export const isTimelineWorkRow = (r: TransactionInfo.CallStackKeyValueMap): boolean => {
  if (!r.begin || r.excludeFromTimeline) {
    return false;
  }
  if (r.isMethod === false) {
    return false;
  }
  return !(r.apiType === 'ASYNC' || Number(r.methodType) === 200);
};

export type ParallelGroup = { start: number; end: number; size: number };
export type ParallelInfo = Map<string, { group: ParallelGroup; isFirst: boolean }>;

/**
 * Detects parallel sibling groups: consecutive children of the same parent whose time windows
 * overlap (a sibling that starts before the running group's max-end was executing concurrently).
 * Each member of a group of >= 2 maps to the shared group window, used to draw a "parallel lane".
 * Sequential (sync) siblings never overlap, so this is a no-op for non-async traces.
 * Async-invocation dividers and metadata rows (annotations / exception details) are skipped.
 */
export const computeParallelGroups = (
  rows: TransactionInfo.CallStackKeyValueMap[] | undefined,
): ParallelInfo => {
  if (!rows?.length) {
    return new Map();
  }

  const childrenByParent = new Map<string, TransactionInfo.CallStackKeyValueMap[]>();
  rows.forEach((r) => {
    if (!isTimelineWorkRow(r)) {
      return; // separators and annotation rows are not timeline work
    }
    const pid = String(r.parentId);
    const list = childrenByParent.get(pid);
    if (list) {
      list.push(r);
    } else {
      childrenByParent.set(pid, [r]);
    }
  });

  if (!childrenByParent.size) {
    return new Map();
  }

  const result: ParallelInfo = new Map();
  childrenByParent.forEach((children) => {
    let i = 0;
    while (i < children.length) {
      const members = [children[i]];
      let maxEnd = rowEndMs(children[i]);
      let j = i + 1;
      // a sibling joins the group while it starts before the group's running max-end
      while (j < children.length && rowStartMs(children[j]) < maxEnd) {
        members.push(children[j]);
        maxEnd = Math.max(maxEnd, rowEndMs(children[j]));
        j++;
      }
      if (members.length >= 2) {
        const start = Math.min(...members.map(rowStartMs));
        const group: ParallelGroup = { start, end: maxEnd, size: members.length };
        members.forEach((m, idx) => result.set(String(m.id), { group, isFirst: idx === 0 }));
      }
      i = j;
    }
  });

  return result;
};
