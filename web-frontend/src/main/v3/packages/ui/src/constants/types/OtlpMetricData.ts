export namespace OtlpMetricData {
  export interface Body {
    applicationName: string;
    metricGroupName: string;
    agentId?: string;
    metricName: string;
    tagGroupList: string[];
    from: number;
    to: number;
    chartType?: string;
    aggregationFunction?: string;
    fieldNameList: string[];
    primaryForFieldAndTagRelation: 'tag' | 'field';
  }

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
    samplingInterval?: number;
  }

  export interface Response {
    timestamp: number[];
    chartType: string;
    unit: string;
    metricValues: MetricValue[];
  }

  export interface MetricValue {
    legendName: string;
    values: number[];
    version?: string;
  }
}
