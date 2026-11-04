import { filterChildItemsByName } from './filterChildItemsByName';

const item = (name: string) => ({ name, path: `/config/service#${name}` });

describe('filterChildItemsByName', () => {
  const items = [item('DEFAULT'), item('order-api'), item('Payment'), item('ORDER-batch')];

  test('returns every item when the query is empty or blank', () => {
    expect(filterChildItemsByName(items, '')).toBe(items);
    expect(filterChildItemsByName(items, '   ')).toBe(items);
  });

  test('matches a part of the name, ignoring case and surrounding spaces', () => {
    expect(filterChildItemsByName(items, ' order ').map(({ name }) => name)).toEqual([
      'order-api',
      'ORDER-batch',
    ]);
  });

  test('keeps the original order', () => {
    expect(filterChildItemsByName(items, 'a').map(({ name }) => name)).toEqual([
      'DEFAULT',
      'order-api',
      'Payment',
      'ORDER-batch',
    ]);
  });

  test('returns an empty list when nothing matches', () => {
    expect(filterChildItemsByName(items, 'zzz')).toEqual([]);
  });
});
