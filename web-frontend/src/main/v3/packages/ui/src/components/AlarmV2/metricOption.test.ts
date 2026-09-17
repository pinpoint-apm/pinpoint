import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import { applyMetric, makeDefaultLeaf, MetricOption } from './metricOption';

// The server rejects a leaf without these, and nothing on screen says which one is missing.
describe('makeDefaultLeaf', () => {
  it('picks MAX when the metric allows several aggregations', () => {
    const metric: MetricOption = {
      value: 'heap_usage_rate',
      label: 'Heap',
      allowedAggregations: ['AVG', 'MAX', 'MIN'],
    };
    expect(makeDefaultLeaf(metric).aggregation).toBe('MAX');
  });

  it('falls back to the first aggregation when MAX is not allowed', () => {
    const metric: MetricOption = { value: 'x', label: 'x', allowedAggregations: ['P95', 'P99'] };
    expect(makeDefaultLeaf(metric).aggregation).toBe('P95');
  });

  it('carries the trigger of a trigger metric', () => {
    const metric: MetricOption = {
      value: 'deadlock_count',
      label: 'Deadlock',
      trigger: 'NEW_GROUP',
    };
    expect(makeDefaultLeaf(metric)).toMatchObject({
      metric: 'deadlock_count',
      trigger: 'NEW_GROUP',
      op: '>',
      threshold: 0,
    });
    expect(makeDefaultLeaf(metric)).not.toHaveProperty('windowSec');
  });
});

describe('applyMetric', () => {
  it('drops an aggregation the new metric does not take', () => {
    const leaf: AlarmV2Rule.AlarmCondition = {
      type: AlarmV2Rule.ConditionType.LEAF,
      metric: 'heap_usage_rate',
      aggregation: 'MAX',
      op: '>=',
      threshold: 80,
      windowSec: 300,
    };
    const next = applyMetric(leaf, { value: 'error_count', label: 'Errors' });
    expect(next).not.toHaveProperty('aggregation');
    expect(next).toMatchObject({ metric: 'error_count', op: '>=', threshold: 80, windowSec: 300 });
  });

  it('drops the trigger condition when switching to an ordinary metric', () => {
    const leaf: AlarmV2Rule.AlarmCondition = {
      type: AlarmV2Rule.ConditionType.LEAF,
      metric: 'deadlock_count',
      trigger: 'NEW_GROUP',
      op: '>',
      threshold: 0,
    };
    const next = applyMetric(leaf, { value: 'error_count', label: 'Errors' });
    expect(next).toMatchObject({
      metric: 'error_count',
      op: '>=',
      threshold: 100,
      windowSec: 3600,
    });
    expect(next.trigger).toBeUndefined();
  });
});
