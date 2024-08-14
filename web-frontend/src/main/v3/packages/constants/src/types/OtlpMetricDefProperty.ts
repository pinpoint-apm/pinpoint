export namespace OtlpMetricDefProperty {
  export interface Response {
    metricGroupList: MetricGroupList[];
    chartTypeList: string[];
    aggregationFunctionList: string[];
  }

  export interface Parameters {
    applicationName: string;
  }

  export interface MetricGroupList {
    metricGroupName: string;
    metricList: MetricList[];
  }

  export interface MetricList {
    metricName: string;
    tagClusterList: TagCluster[];
  }

  export interface TagCluster {
    tags: string;
    fieldAndUnitList: FieldAndUnit[];
  }

  export interface FieldAndUnit {
    fieldName: string;
    unit: string;
  }
}
