/* eslint-disable  @typescript-eslint/no-explicit-any */
export namespace SystemMetricChart {
  export interface Parameters {
    hostGroupName: string;
    hostName: string;
    metricDefinitionId: string;
    from: number;
    to: number;
    tags?: string;
  }

  export interface Response {
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
}
