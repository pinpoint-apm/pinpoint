import { TFunction } from 'i18next';
import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';

/** Compact, locale-neutral duration for dense table cells: 30s / 5m / 1h. */
export const formatIntervalShort = (sec?: number) => {
  if (!sec && sec !== 0) return '';
  if (sec % 3600 === 0) return `${sec / 3600}h`;
  if (sec % 60 === 0) return `${sec / 60}m`;
  return `${sec}s`;
};

// Windowless leaves (NEW_GROUP) are collected as undefined rather than skipped, or a tree of
// [1h leaf, NEW_GROUP leaf] would look uniform and get one trailing "in 1h".
const collectWindowSecs = (
  condition: AlarmV2Rule.AlarmCondition | undefined,
  windowSecs: Set<number | undefined>,
) => {
  if (!condition) return;
  if (condition.type === AlarmV2Rule.ConditionType.GROUP) {
    condition.criteria?.forEach((child) => collectWindowSecs(child, windowSecs));
    return;
  }
  windowSecs.add(condition.windowSec);
};

const collectMetrics = (condition: AlarmV2Rule.AlarmCondition | undefined, metrics: string[]) => {
  if (!condition) return;
  if (condition.type === AlarmV2Rule.ConditionType.GROUP) {
    condition.criteria?.forEach((child) => collectMetrics(child, metrics));
    return;
  }
  if (condition.metric && !metrics.includes(condition.metric)) {
    metrics.push(condition.metric);
  }
};

/** Distinct metrics a rule watches, in tree order. */
export const conditionMetrics = (condition?: AlarmV2Rule.AlarmCondition) => {
  const metrics: string[] = [];
  collectMetrics(condition, metrics);
  return metrics;
};

type MetricLabel = (metric: string) => string;
const identity: MetricLabel = (metric) => metric;

const expression = (
  condition: AlarmV2Rule.AlarmCondition | undefined,
  withWindow: boolean,
  metricLabel: MetricLabel,
): string => {
  if (!condition) return '';
  if (condition.type === AlarmV2Rule.ConditionType.GROUP) {
    const operator = condition.operator ?? 'AND';
    return (condition.criteria ?? [])
      .map((child) => {
        const text = expression(child, withWindow, metricLabel);
        return child.type === AlarmV2Rule.ConditionType.GROUP ? `(${text})` : text;
      })
      .filter(Boolean)
      .join(` ${operator} `);
  }
  const leaf = [
    condition.metric && metricLabel(condition.metric),
    condition.op,
    condition.threshold,
  ].filter((part) => part !== undefined && part !== '');
  const text = leaf.join(' ');
  // A NEW_GROUP leaf carries no window, so there is nothing to stamp on it.
  return withWindow && condition.windowSec !== undefined
    ? `${text} [${formatIntervalShort(condition.windowSec)}]`
    : text;
};

/**
 * One-line summary for list rows, e.g. `error_count >= 100 in 1h`. The window is the
 * range the metric is aggregated over, ending at the moment the rule is evaluated.
 * Leaves share a single trailing window when they agree; otherwise each carries its own.
 */
export const formatCondition = (
  condition?: AlarmV2Rule.AlarmCondition,
  metricLabel: MetricLabel = identity,
  t?: TFunction,
) => {
  const windowSecs = new Set<number | undefined>();
  collectWindowSecs(condition, windowSecs);
  const [onlyWindowSec] = [...windowSecs];
  const uniformWindowSec = windowSecs.size === 1 ? onlyWindowSec : undefined;
  const text = expression(condition, uniformWindowSec === undefined, metricLabel);
  if (!text) return '';
  if (uniformWindowSec === undefined) return text;
  const window = formatIntervalShort(uniformWindowSec);
  return t
    ? `${text} ${t('CONFIGURATION.ALARM_V2.CONDITION_IN', { window })}`
    : `${text} in ${window}`;
};
