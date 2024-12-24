/* eslint-disable  @typescript-eslint/no-explicit-any */
export interface Chart {
  title: string;
  timestamp: number[];
  metricValueGroups: MetricValueGroup[];
}

export interface MetricValueGroup {
  groupName: string;
  chartType: string;
  unit: string;
  metricValues: MetricValue[];
}

export interface MetricValue {
  fieldName: string;
  values: number[];
  tags?: any[];
}
