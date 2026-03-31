import { renderHook, act } from '@testing-library/react';
import { useLocalStorage, useExpiredLocalStorage } from './useLocalStorage';

describe('useLocalStorage', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  test('returns initial value when localStorage is empty', () => {
    const { result } = renderHook(() => useLocalStorage('test-key', 'default'));
    expect(result.current[0]).toBe('default');
  });

  test('returns stored value from localStorage on mount', () => {
    localStorage.setItem('test-key', JSON.stringify('stored-value'));
    const { result } = renderHook(() => useLocalStorage('test-key', 'default'));
    expect(result.current[0]).toBe('stored-value');
  });

  test('updates localStorage when value changes', async () => {
    const { result } = renderHook(() => useLocalStorage('test-key', 'initial'));

    act(() => {
      result.current[1]('updated');
    });

    expect(result.current[0]).toBe('updated');
    expect(JSON.parse(localStorage.getItem('test-key')!)).toBe('updated');
  });

  test('returns initial value when stored JSON is invalid', () => {
    localStorage.setItem('test-key', 'invalid-json{');
    const { result } = renderHook(() => useLocalStorage('test-key', 'fallback'));
    expect(result.current[0]).toBe('fallback');
  });

  test('returns undefined when stored value is the string "undefined" (usehooks-ts re-parses storage in effect)', () => {
    // usehooks-ts re-reads from storage after mount and JSON.parse('undefined') produces undefined,
    // overriding the factory's 'fallback' initial value.
    localStorage.setItem('test-key', 'undefined');
    const { result } = renderHook(() => useLocalStorage('test-key', 'fallback'));
    expect(result.current[0]).toBeUndefined();
  });

  test('works with object values', () => {
    const initial = { a: 1, b: 'hello' };
    const { result } = renderHook(() => useLocalStorage('test-obj', initial));
    expect(result.current[0]).toEqual(initial);
  });

  test('works with array values', () => {
    const { result } = renderHook(() => useLocalStorage<string[]>('test-arr', []));

    act(() => {
      result.current[1](['x', 'y']);
    });

    expect(result.current[0]).toEqual(['x', 'y']);
  });
});

describe('useExpiredLocalStorage', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  test('returns undefined when no value is stored', () => {
    const { result } = renderHook(() => useExpiredLocalStorage('test-expired'));
    expect(result.current[0]).toBeUndefined();
  });

  test('keeps value when it has not expired', () => {
    const futureExpire = Date.now() + 60_000;
    const stored = { value: 'live', expire: futureExpire };
    localStorage.setItem('test-expired', JSON.stringify(stored));

    const { result } = renderHook(() => useExpiredLocalStorage('test-expired'));
    expect(result.current[0]?.value).toBe('live');
  });

  test('removes value from localStorage when it has expired', () => {
    const pastExpire = Date.now() - 1;
    localStorage.setItem('test-expired', JSON.stringify({ value: 'stale', expire: pastExpire }));

    renderHook(() => useExpiredLocalStorage('test-expired'));

    expect(localStorage.getItem('test-expired')).toBeNull();
  });
});
