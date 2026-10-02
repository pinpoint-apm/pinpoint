import type { TFunction } from 'i18next';
import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import { conditionMetrics, formatCondition, formatIntervalShort } from './formatCondition';

const leaf = (over: Partial<AlarmV2Rule.AlarmCondition> = {}): AlarmV2Rule.AlarmCondition => ({
  type: AlarmV2Rule.ConditionType.LEAF,
  metric: 'error_count',
  op: '>=',
  threshold: 100,
  windowSec: 3600,
  ...over,
});

const group = (
  criteria: AlarmV2Rule.AlarmCondition[],
  operator: 'AND' | 'OR' = 'AND',
): AlarmV2Rule.AlarmCondition => ({
  type: AlarmV2Rule.ConditionType.GROUP,
  operator,
  criteria,
});

const t = ((key: string, options?: { window?: string }) =>
  key.endsWith('CONDITION_IN') ? `in ${options?.window}` : key) as unknown as TFunction;

describe('formatIntervalShort', () => {
  it.each([
    [3600, '1h'],
    [7200, '2h'],
    [60, '1m'],
    [300, '5m'],
    [90, '90s'],
    [undefined, ''],
  ])('renders %s as %s', (sec, expected) => {
    expect(formatIntervalShort(sec)).toBe(expected);
  });
});

describe('conditionMetrics', () => {
  it('collects distinct metrics in tree order and skips blank ones', () => {
    const tree = group([
      leaf({ metric: 'error_count' }),
      leaf({ metric: '' }),
      group([leaf({ metric: 'affected_user_count' }), leaf({ metric: 'error_count' })]),
    ]);

    expect(conditionMetrics(tree)).toEqual(['error_count', 'affected_user_count']);
  });

  it('returns nothing for an undefined condition', () => {
    expect(conditionMetrics(undefined)).toEqual([]);
  });
});

describe('formatCondition', () => {
  it('returns an empty string for no condition', () => {
    expect(formatCondition(undefined, undefined, t)).toBe('');
  });

  it('hangs a single shared window on the end', () => {
    const tree = group([leaf({ metric: 'a' }), leaf({ metric: 'b' })]);

    expect(formatCondition(tree, undefined, t)).toBe('a >= 100 AND b >= 100 in 1h');
  });

  it('stamps each leaf when the windows differ', () => {
    const tree = group([leaf({ metric: 'a' }), leaf({ metric: 'b', windowSec: 300 })]);

    expect(formatCondition(tree, undefined, t)).toBe('a >= 100 [1h] AND b >= 100 [5m]');
  });

  it('does not claim a shared window when a leaf has none', () => {
    const tree = group([
      leaf({ metric: 'a' }),
      leaf({ metric: 'b', trigger: 'NEW_GROUP', windowSec: undefined }),
    ]);

    const result = formatCondition(tree, undefined, t);

    expect(result).toBe('a >= 100 [1h] AND b >= 100');
    expect(result).not.toMatch(/in 1h$/);
  });

  it('keeps a threshold of 0 instead of dropping it', () => {
    expect(formatCondition(leaf({ metric: 'a', threshold: 0 }), undefined, t)).toBe('a >= 0 in 1h');
  });

  it('labels metrics through the supplied lookup', () => {
    expect(formatCondition(leaf({ metric: 'error_count' }), () => 'Error Count', t)).toBe(
      'Error Count >= 100 in 1h',
    );
  });

  it('falls back to English when no translator is given', () => {
    expect(formatCondition(leaf({ metric: 'a' }))).toBe('a >= 100 in 1h');
  });

  it('joins a group with its operator', () => {
    const tree = group([leaf({ metric: 'a' }), leaf({ metric: 'b' })], 'OR');

    expect(formatCondition(tree, undefined, t)).toBe('a >= 100 OR b >= 100 in 1h');
  });
});
