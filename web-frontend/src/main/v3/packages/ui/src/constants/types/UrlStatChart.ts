/* eslint-disable  @typescript-eslint/no-explicit-any */
export namespace UrlStatChartType {
  export interface Parameters {
    applicationName: string;
    from: number;
    to: number;
    uri: string;
    type: string;
    agentId?: string;
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
