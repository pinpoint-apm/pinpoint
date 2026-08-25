import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { getCallTreeRowClasses } from './rowClasses';

// The backend sends `id` as a number inside the raw call stack arrays, so the row data carries a
// number while every prop that points at a row carries a string. The tests below keep it that way
// on purpose — matching only when the types happen to line up is the bug this file guards.
const row = (r: { id: number | string; hasException?: boolean }) =>
  ({ hasException: false, ...r }) as unknown as TransactionInfo.CallStackKeyValueMap;

// The component hands the result to `cn()`, so assert on the joined string the row ends up with.
const classesOf = (...args: Parameters<typeof getCallTreeRowClasses>) =>
  getCallTreeRowClasses(...args).join(' ');

describe('getCallTreeRowClasses', () => {
  test('marks a plain row with nothing but the hover group', () => {
    expect(getCallTreeRowClasses(row({ id: 9 }), {})).toEqual(['group']);
  });

  test('marks every search match', () => {
    expect(classesOf(row({ id: 9 }), { filteredRowIds: ['8', '9'] })).toContain('bg-yellow-100');
    expect(classesOf(row({ id: 7 }), { filteredRowIds: ['8', '9'] })).not.toContain(
      'bg-yellow-100',
    );
  });

  test('marks the search hit the user is standing on, with a numeric row id', () => {
    expect(
      classesOf(row({ id: 9 }), { filteredRowIds: ['8', '9'], highlightRowId: '9' }),
    ).toContain('bg-yellow-200');
  });

  test('marks only the row the caller picked', () => {
    const marks = { filteredRowIds: ['8', '9'], highlightRowId: '8' };
    expect(classesOf(row({ id: 8 }), marks)).toContain('bg-yellow-200');
    expect(classesOf(row({ id: 9 }), marks)).not.toContain('bg-yellow-200');
  });

  test('leaves the mark to plain hover styling, with no hover override', () => {
    expect(classesOf(row({ id: 9 }), { highlightRowId: '9' })).not.toContain('hover:');
  });

  test('marks nothing once the search is cancelled', () => {
    expect(getCallTreeRowClasses(row({ id: 9 }), { highlightRowId: undefined })).toEqual(['group']);
  });

  test('marks an exception row', () => {
    expect(classesOf(row({ id: 9, hasException: true }), {})).toContain('bg-rose-50');
  });

  test('marks nothing when no row was picked', () => {
    expect(getCallTreeRowClasses(row({ id: '' }), {})).toEqual(['group']);
  });
});
