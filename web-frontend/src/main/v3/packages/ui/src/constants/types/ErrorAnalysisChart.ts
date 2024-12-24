/* eslint-disable  @typescript-eslint/no-explicit-any */
export namespace ErrorAnalysisChartType {
  export interface Parameters {
    applicationName: string;
    from: number;
    to: number;
    agentId?: string;
    groupBy?: string;
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
