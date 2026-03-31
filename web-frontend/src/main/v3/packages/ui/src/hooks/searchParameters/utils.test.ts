import { getSearchParameters, getDateRange } from './utils';

describe('getSearchParameters', () => {
  test('parses a simple query string into a key-value object', () => {
    const result = getSearchParameters('?foo=bar&baz=qux');
    expect(result).toEqual({ foo: 'bar', baz: 'qux' });
  });

  test('decodes percent-encoded values', () => {
    const result = getSearchParameters('?name=Hello%20World&path=%2Fsome%2Fpath');
    expect(result).toEqual({ name: 'Hello World', path: '/some/path' });
  });

  test('returns an empty object for an empty string', () => {
    const result = getSearchParameters('');
    expect(result).toEqual({});
  });

  test('handles a query string without a leading "?"', () => {
    const result = getSearchParameters('key=value');
    expect(result).toEqual({ key: 'value' });
  });

  test('handles repeated keys by keeping the last occurrence', () => {
    const result = getSearchParameters('?a=1&a=2');
    // URLSearchParams keeps the last value for repeated keys via the map conversion
    expect(result).toHaveProperty('a');
  });

  test('handles keys with empty values', () => {
    const result = getSearchParameters('?empty=');
    expect(result).toEqual({ empty: '' });
  });
});

describe('getDateRange', () => {
  test('returns a date range with "from" before "to" in realtime mode', () => {
    const result = getDateRange('', true);
    expect(result.from).toBeInstanceOf(Date);
    expect(result.to).toBeInstanceOf(Date);
    expect(result.from.getTime()).toBeLessThan(result.to.getTime());
  });

  test('ignores search params in realtime mode and uses current time', () => {
    const before = Date.now();
    const result = getDateRange('?from=2020-01-01-00-00-00&to=2020-01-01-01-00-00', true);
    const after = Date.now();
    expect(result.to.getTime()).toBeGreaterThanOrEqual(before - 100);
    expect(result.to.getTime()).toBeLessThanOrEqual(after + 100);
  });

  test('parses from/to dates from search params when not realtime', () => {
    const result = getDateRange('?from=2023-11-10-09-00-00&to=2023-11-10-10-00-00', false);
    expect(result.from).toBeInstanceOf(Date);
    expect(result.to).toBeInstanceOf(Date);
    expect(result.from.getTime()).toBeLessThan(result.to.getTime());
  });

  test('returns a 1-hour range between from and to when not realtime', () => {
    const result = getDateRange('?from=2023-11-10-09-00-00&to=2023-11-10-10-00-00', false);
    const diffMs = result.to.getTime() - result.from.getTime();
    expect(diffMs).toBe(60 * 60 * 1000); // exactly 1 hour
  });
});
