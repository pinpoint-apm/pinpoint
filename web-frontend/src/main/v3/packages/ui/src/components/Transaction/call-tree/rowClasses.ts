import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';

/**
 * Row background marks for the Call Tree, as a pure function so the rules between them can be
 * unit-tested. Kept free of React / component imports for the same reason as `timeline.ts`.
 */
export interface CallTreeRowMarks {
  /** Every row the current search matched. */
  filteredRowIds?: string[];
  /**
   * The single "you are here" row: the initial `focusCallStackId` row, the row just stepped into,
   * or the search hit the user is standing on. Cleared with the search.
   */
  highlightRowId?: string;
}

export const getCallTreeRowClasses = (
  row: TransactionInfo.CallStackKeyValueMap,
  { filteredRowIds, highlightRowId }: CallTreeRowMarks,
): string[] => {
  // `group` lets row-hover reveal the per-row actions rendered inside the cells.
  const classes = ['group'];

  // `id` is typed `any` and arrives from the backend as a number, so every comparison here goes
  // through String() on both sides — `"9" === 9` is false and silently drops the mark.
  const rowId = String(row.id);

  if (row.hasException) {
    classes.push('bg-rose-50');
  }
  if (filteredRowIds?.some((id) => String(id) === rowId)) {
    classes.push('bg-yellow-100');
  }
  if (highlightRowId !== undefined && rowId === String(highlightRowId)) {
    classes.push('bg-yellow-200');
  }

  return classes;
};
