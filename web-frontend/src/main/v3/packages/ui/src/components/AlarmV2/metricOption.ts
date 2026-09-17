import { AlarmV2Catalog, AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';

export type MetricOption = AlarmV2Catalog.MetricDefinition;

// The catalog sends allowed aggregations as a set, so its order is not stable; MAX is what v1 did.
export const getDefaultAggregation = (metric?: MetricOption) => {
  const allowed = metric?.allowedAggregations;
  return allowed?.includes('MAX') ? 'MAX' : allowed?.[0];
};

/** Points a leaf at a metric, filling in what the metric dictates: aggregation and trigger. */
export const applyMetric = (
  leaf: AlarmV2Rule.AlarmCondition,
  metric?: MetricOption,
): AlarmV2Rule.AlarmCondition => {
  const next = { ...leaf };
  delete next.aggregation;
  const aggregation = getDefaultAggregation(metric);

  return {
    ...next,
    metric: metric?.value ?? leaf.metric,
    ...(aggregation && { aggregation }),
    trigger: metric?.trigger,
    op: metric?.trigger ? '>' : (leaf.op ?? '>='),
    threshold: metric?.trigger ? 0 : (leaf.threshold ?? 100),
  };
};

export const makeDefaultLeaf = (metric?: MetricOption): AlarmV2Rule.AlarmCondition =>
  applyMetric(
    { type: AlarmV2Rule.ConditionType.LEAF, metric: 'error_count', windowSec: 3600 },
    metric,
  );
