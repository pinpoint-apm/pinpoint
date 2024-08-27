export namespace OtlpMetricData {
  export interface Parameters {
    applicationName: string;
    metricGroupName: string;
    metricName: string;
    from: number;
    to: number;
    tags?: string;
    chartType?: string;
    aggregationFunction?: string;
    fieldNameList?: string;
    agentId?: string;
  }

  export interface Response {
    timestamp: number[];
    chartType: string;
    unit: string;
    metricValues: MetricValue[];
  }

  export interface MetricValue {
    legendName: string;
    valueList: number[];
    version?: string;
  }
}
