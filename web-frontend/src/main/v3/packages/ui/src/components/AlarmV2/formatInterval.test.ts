import type { TFunction } from 'i18next';
import {
  ACTION_INTERVAL_SEC_OPTIONS,
  CHECK_INTERVAL_SEC_OPTIONS,
  formatIntervalSec,
  roundUpActionIntervalSec,
  roundUpCheckIntervalSec,
} from './formatInterval';

// Echo the key and interpolation so the branch taken is visible in the assertion.
const t = ((key: string, options?: { value?: number }) =>
  `${key.split('.').pop()}:${options?.value}`) as unknown as TFunction;

describe('formatIntervalSec', () => {
  it.each([
    [3600, 'INTERVAL_HOUR:1'],
    [86400, 'INTERVAL_HOUR:24'],
    [60, 'INTERVAL_MINUTE:1'],
    [1800, 'INTERVAL_MINUTE:30'],
    [90, 'INTERVAL_SECOND:90'],
    [30, 'INTERVAL_SECOND:30'],
  ])('renders %i through the matching unit', (sec, expected) => {
    expect(formatIntervalSec(sec, t)).toBe(expected);
  });

  // 0 is divisible by everything, so it takes the first branch rather than the last.
  it('treats 0 as hours', () => {
    expect(formatIntervalSec(0, t)).toBe('INTERVAL_HOUR:0');
  });
});

describe('roundUpCheckIntervalSec', () => {
  // The options are what the select can render, so every option must survive
  // untouched -- otherwise opening a saved rule would silently shift its interval.
  it('leaves every option unchanged', () => {
    for (const option of CHECK_INTERVAL_SEC_OPTIONS) {
      expect(roundUpCheckIntervalSec(option)).toBe(option);
    }
  });

  it.each([
    [1, 60],
    [59, 60],
    [61, 180],
    [179, 180],
    [181, 300],
    [299, 300],
    [301, 600],
    [1799, 1800],
  ])('rounds %i up to the next option (%i)', (sec, expected) => {
    expect(roundUpCheckIntervalSec(sec)).toBe(expected);
  });

  // Above the last option the value is kept as is: the server rejects it rather than
  // shortening it, so shortening here would hide the error instead of surfacing it.
  it.each([3601, 86400, Number.MAX_SAFE_INTEGER])('keeps %i, which is above the last option', (sec) => {
    expect(roundUpCheckIntervalSec(sec)).toBe(sec);
  });

  it('rounds a non-positive value up to the smallest option', () => {
    expect(roundUpCheckIntervalSec(0)).toBe(60);
    expect(roundUpCheckIntervalSec(-1)).toBe(60);
  });
});

describe('roundUpActionIntervalSec', () => {
  it('leaves every option unchanged', () => {
    for (const option of ACTION_INTERVAL_SEC_OPTIONS) {
      expect(roundUpActionIntervalSec(option)).toBe(option);
    }
  });

  it('rounds up to the next option', () => {
    expect(roundUpActionIntervalSec(1)).toBe(300);
    expect(roundUpActionIntervalSec(3601)).toBe(21600);
  });
});
